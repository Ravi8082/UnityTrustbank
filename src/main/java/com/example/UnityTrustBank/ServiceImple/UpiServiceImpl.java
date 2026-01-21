package com.example.UnityTrustBank.ServiceImple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.*;
import com.example.UnityTrustBank.Enum.*;
import com.example.UnityTrustBank.Repository.*;
import com.example.UnityTrustBank.Service.UpiService;

@Service
@Transactional
public class UpiServiceImpl implements UpiService {

    @Autowired private UpiRepo upiRepo;
    @Autowired private AccountRepo accountRepo;
    @Autowired private TransactionRepo transactionRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private LedgerService ledgerService;
    @Autowired private UpiUsageRepo upiUsageRepo;

    private static final BigDecimal MAX_TXN_LIMIT = BigDecimal.valueOf(100_000);
    private static final BigDecimal DAILY_UPI_LIMIT = BigDecimal.valueOf(200_000);

    // ================= CREATE UPI =================
    @Override
    public void createUpi(Long accountId, String vpa, String pin) {

        if (vpa == null || !vpa.matches("^[a-zA-Z0-9._]+@utb$"))
            throw new RuntimeException("Invalid VPA format. Example: ravi@utb");

        if (pin == null || !pin.matches("\\d{4}"))
            throw new RuntimeException("UPI PIN must be exactly 4 digits");

        if (upiRepo.existsByVpa(vpa))
            throw new RuntimeException("VPA already exists");

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        User current = getCurrentUser();

        if (!account.getUser().getId().equals(current.getId()))
            throw new RuntimeException("Unauthorized account access");

        if (account.getStatus() != AccountStatus.ACTIVE)
            throw new RuntimeException("Account not active");

        if (upiRepo.existsByAccount_Id(accountId))
            throw new RuntimeException("UPI already linked with this account");

        UpiAccount upi = new UpiAccount();
        upi.setVpa(vpa);
        upi.setPinHash(passwordEncoder.encode(pin));
        upi.setStatus(UpiStatus.ACTIVE);
        upi.setCreatedAt(LocalDateTime.now());
        upi.setAccount(account);

        upiRepo.save(upi);

        emailService.send(
                current.getEmail(),
                "UnityTrust Bank – UPI Created",
                "Dear Customer,\n\n" +
                "Your UPI ID has been successfully created.\n" +
                "VPA: " + vpa + "\n\n" +
                "If you did not request this, contact support immediately.\n\n" +
                "Regards,\nUnityTrust Bank"
        );
    }

    // ================= UPI PAYMENT =================
    @Override
    public void pay(String fromVpa, String toVpa, String pin, BigDecimal amount, String remark) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Invalid amount");

        if (amount.compareTo(MAX_TXN_LIMIT) > 0)
            throw new RuntimeException("Transaction exceeds per transaction limit");

        if (fromVpa.equals(toVpa))
            throw new RuntimeException("Self transfer not allowed");

        UpiAccount sender = upiRepo.findByVpa(fromVpa)
                .orElseThrow(() -> new RuntimeException("Sender UPI not found"));

        UpiAccount receiver = upiRepo.findByVpa(toVpa)
                .orElseThrow(() -> new RuntimeException("Receiver UPI not found"));

        User current = getCurrentUser();

        if (!sender.getAccount().getUser().getId().equals(current.getId()))
            throw new RuntimeException("Unauthorized UPI access");

        if (!passwordEncoder.matches(pin, sender.getPinHash()))
            throw new RuntimeException("Invalid UPI PIN");

        // Lock accounts
        Account from = accountRepo.findByIdForUpdate(sender.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Sender account missing"));

        Account to = accountRepo.findByIdForUpdate(receiver.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Receiver account missing"));

        if (from.getStatus() != AccountStatus.ACTIVE)
            throw new RuntimeException("Sender account not active");

        if (to.getStatus() != AccountStatus.ACTIVE)
            throw new RuntimeException("Receiver account not active");

        if (from.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        // Daily limit check
        UpiUsage usage = upiUsageRepo.findByUserIdAndDate(current.getId(), LocalDate.now())
                .orElseGet(() -> {
                    UpiUsage u = new UpiUsage();
                    u.setUserId(current.getId());
                    u.setDate(LocalDate.now());
                    u.setTotalUsed(BigDecimal.ZERO);
                    return u;
                });

        if (usage.getTotalUsed().add(amount).compareTo(DAILY_UPI_LIMIT) > 0)
            throw new RuntimeException("Daily UPI limit exceeded");

        String ref = UUID.randomUUID().toString();

        // Ledger entry
        BigDecimal senderNewBalance = ledgerService.postEntry(from, TransactionType.DEBIT, amount,
                "UPI", "UPI to " + toVpa, ref);

        BigDecimal receiverNewBalance = ledgerService.postEntry(to, TransactionType.CREDIT, amount,
                "UPI", "UPI from " + fromVpa, ref);

        // Update daily usage
        usage.setTotalUsed(usage.getTotalUsed().add(amount));
        upiUsageRepo.save(usage);

        // Record transactions
        Transaction debit = new Transaction();
        debit.setAccount(from);
        debit.setType(TransactionType.DEBIT);
        debit.setChannel(TransactionChannel.UPI);
        debit.setAmount(amount);
        debit.setBalanceAfter(senderNewBalance);
        debit.setReferenceNo(ref);
        debit.setRemark("UPI to " + toVpa);
        debit.setTransactionTime(LocalDateTime.now());

        Transaction credit = new Transaction();
        credit.setAccount(to);
        credit.setType(TransactionType.CREDIT);
        credit.setChannel(TransactionChannel.UPI);
        credit.setAmount(amount);
        credit.setBalanceAfter(receiverNewBalance);
        credit.setReferenceNo(ref);
        credit.setRemark("UPI from " + fromVpa);
        credit.setTransactionTime(LocalDateTime.now());

        transactionRepo.save(debit);
        transactionRepo.save(credit);

        // Email notification
        emailService.send(
                current.getEmail(),
                "UnityTrust Bank – UPI Payment Successful",
                "Dear Customer,\n\n" +
                "Your UPI payment was successful.\n" +
                "Amount: ₹" + amount + "\n" +
                "To: " + toVpa + "\n" +
                "Reference: " + ref + "\n\n" +
                "Remarks: " + remark + "\n\n" +
                "Regards,\nUnityTrust Bank"
        );
    }

    // ================= HELPERS =================
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
