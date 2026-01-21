package com.example.UnityTrustBank.ServiceImple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.*;
import com.example.UnityTrustBank.Enum.*;
import com.example.UnityTrustBank.Repository.*;
import com.example.UnityTrustBank.Service.AtmService;

@Service
@Transactional
public class AtmServiceImpl implements AtmService {

    @Autowired private AccountRepo accountRepo;
    @Autowired private AtmRepo atmRepo;
    @Autowired private AtmRequestRepo atmRequestRepo;
    @Autowired private AtmReceiptRepo atmReceiptRepo;
    @Autowired private AtmPinHistoryRepo pinHistoryRepo;
    @Autowired private TransactionRepo transactionRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private LedgerService ledgerService;

    // ================= ATM REQUEST =================

    @Override
    public void requestAtm(String accountNumber) {

        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        User current = getCurrentUser();

        if (!account.getUser().getId().equals(current.getId()))
            throw new RuntimeException("Unauthorized ATM request");

        if (account.getStatus() != AccountStatus.ACTIVE)
            throw new RuntimeException("Account not active");

        if (atmRepo.existsByAccount_Id(account.getId()))
            throw new RuntimeException("ATM already issued");

        AtmRequest req = new AtmRequest();
        req.setAccount(account);
        req.setRequestedBy(current);
        req.setStatus(Request.PENDING);
        req.setRequestDate(LocalDateTime.now());

        atmRequestRepo.save(req);
    }

    // ================= ATM APPROVAL =================

    @Override
    public void approveAtm(Long requestId) {

        AtmRequest req = atmRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != Request.PENDING)
            throw new RuntimeException("Already processed");

        Account account = req.getAccount();

        if (!account.getBranch().isActive())
            throw new RuntimeException("Branch inactive");

        String cardNumber = generateUniqueCardNumber();
        String pin = generatePin();
        String cvv = generateCvv();

        AtmCard card = new AtmCard();
        card.setAccount(account);
        card.setCardNumber(cardNumber);
        card.setPinHash(passwordEncoder.encode(pin));
        card.setCvvHash(passwordEncoder.encode(cvv));
        card.setIssuedAt(LocalDateTime.now());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setStatus(AtmStatus.ACTIVE);
        card.setFailedPinAttempts(0);
        card.setDailyWithdrawalLimit(BigDecimal.valueOf(20000));
        card.setDailyWithdrawnAmount(BigDecimal.ZERO);

        atmRepo.save(card);

        req.setStatus(Request.APPROVED);
        req.setApprovedDate(LocalDateTime.now());
        req.setApprovedBy(getCurrentUser());
        atmRequestRepo.save(req);
    }

    // ================= ATM WITHDRAW =================

    @Override
    public void withdraw(Long cardId, String pin, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Invalid amount");

        AtmCard card = atmRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("ATM card not found"));

        validateCard(card, pin);

        if (card.getExpiryDate().isBefore(LocalDate.now()))
            throw new RuntimeException("ATM card expired");

        if (!LocalDate.now().equals(card.getLastWithdrawalDate())) {
            card.setDailyWithdrawnAmount(BigDecimal.ZERO);
            card.setLastWithdrawalDate(LocalDate.now());
        }

        if (card.getDailyWithdrawnAmount().add(amount)
                .compareTo(card.getDailyWithdrawalLimit()) > 0)
            throw new RuntimeException("Daily withdrawal limit exceeded");

        Account account = accountRepo.findByIdForUpdate(card.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        String ref = UUID.randomUUID().toString();

        BigDecimal newBalance = ledgerService.postEntry(
                account,
                TransactionType.DEBIT,
                amount,
                "ATM",
                "ATM Withdrawal",
                ref
        );
        AtmReceipt receipt = new AtmReceipt();
        receipt.setReceiptNo(UUID.randomUUID().toString());
        receipt.setAtmId("ATM-" + account.getBranch().getBranchCode());
        receipt.setBranchCode(account.getBranch().getBranchCode());
        receipt.setAmount(amount);
        receipt.setTime(LocalDateTime.now());
        receipt.setAccount(account);
        atmReceiptRepo.save(receipt);


        card.setDailyWithdrawnAmount(card.getDailyWithdrawnAmount().add(amount));
        atmRepo.save(card);

        Transaction txn = new Transaction();
        txn.setAccount(account);
        txn.setType(TransactionType.DEBIT);
        txn.setChannel(TransactionChannel.ATM);
        txn.setAmount(amount);
        txn.setBalanceAfter(newBalance);
        txn.setReferenceNo(ref);
        txn.setTransactionTime(LocalDateTime.now());

        transactionRepo.save(txn);
    }

    // ================= PIN RESET =================

    @Override
    public void resetPin(Long cardId, String oldPin, String newPin) {

        if (newPin == null || newPin.length() != 4)
            throw new RuntimeException("PIN must be 4 digits");

        AtmCard card = atmRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!passwordEncoder.matches(oldPin, card.getPinHash()))
            throw new RuntimeException("Old PIN incorrect");

        card.setPinHash(passwordEncoder.encode(newPin));
        card.setFailedPinAttempts(0);
        atmRepo.save(card);

        AtmPinHistory history = new AtmPinHistory();
        history.setCardId(cardId);
        history.setChangedAt(LocalDateTime.now());
        pinHistoryRepo.save(history);
    }

    // ================= HELPERS =================

    private void validateCard(AtmCard card, String pin) {

        if (card.getStatus() != AtmStatus.ACTIVE)
            throw new RuntimeException("ATM card inactive");

        if (!passwordEncoder.matches(pin, card.getPinHash())) {
            card.setFailedPinAttempts(card.getFailedPinAttempts() + 1);
            if (card.getFailedPinAttempts() >= 3)
                card.setStatus(AtmStatus.BLOCKED);

            atmRepo.save(card);
            throw new RuntimeException("Invalid PIN");
        }

        card.setFailedPinAttempts(0);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String generateUniqueCardNumber() {
        String card;
        do {
            card = "6149" + (100000000000L +
                    Math.abs(new Random().nextLong() % 900000000000L));
        } while (atmRepo.existsByCardNumber(card));
        return card;
    }

    private String generatePin() {
        return String.valueOf(1000 + new Random().nextInt(9000));
    }

    private String generateCvv() {
        return String.valueOf(100 + new Random().nextInt(900));
    }

    @Override
    public List<AtmRequest> pendingAtmRequests() {
        return atmRequestRepo.findByStatus(Request.PENDING);
    }

    @Override
    public void rejectAtm(Long requestId, String reason) {

        if (reason == null || reason.isBlank())
            throw new RuntimeException("Rejection reason required");

        AtmRequest req = atmRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != Request.PENDING)
            throw new RuntimeException("Already processed");

        req.setStatus(Request.REJECTED);
        req.setRejectionReason(reason);
        req.setApprovedDate(LocalDateTime.now());
        req.setApprovedBy(getCurrentUser());

        atmRequestRepo.save(req);

        emailService.send(
                req.getAccount().getUser().getEmail(),
                "UnityTrust Bank – ATM Request Rejected",
                "Your ATM request has been rejected.\n\nReason: " + reason
        );
       
    }
    public void reissueCard(Long accountId) {
        atmRepo.findByAccount_Id(accountId).ifPresent(card -> {
            card.setStatus(AtmStatus.BLOCKED);
            atmRepo.save(card);
        });

        requestAtm(accountRepo.findById(accountId).get().getAccountNumber());
    }

}
