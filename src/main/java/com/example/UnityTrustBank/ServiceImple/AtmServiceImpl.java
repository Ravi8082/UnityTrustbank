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
import com.example.UnityTrustBank.dto.AtmCardDetailsDto;
import com.example.UnityTrustBank.dto.OtpStore;
import com.example.UnityTrustBank.exception.InsufficientBalanceException;
import com.example.UnityTrustBank.exception.ResourceNotFoundException;
import com.example.UnityTrustBank.exception.UnauthorizedAccessException;

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
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        User current = getCurrentUser();

        if (!account.getUser().getId().equals(current.getId()))
            throw new UnauthorizedAccessException("Unauthorized: You cannot request ATM for someone else's account");

        if (account.getStatus() != AccountStatus.ACTIVE)
            throw new RuntimeException("Account not active");

        if (atmRepo.existsByAccount_Id(account.getId()))
            throw new RuntimeException("ATM already issued");

        // Check if there's already a pending request for this user
        if (hasPendingAtmRequest(current.getId())) {
            throw new RuntimeException("You already have a pending ATM request. Please wait for approval or contact support.");
        }

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

        User current = getCurrentUser();
        
        // Check if current user has admin or manager role and verify branch access
        if ((current.getRole().getRoleName() == AppRole.ROLE_ADMIN || current.getRole().getRoleName() == AppRole.ROLE_MANAGER)) {
            if (!req.getAccount().getBranch().getId()
                    .equals(current.getBranch().getId())) {
                throw new RuntimeException("Unauthorized branch access");
            }
        }

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

        // VERY IMPORTANT
        account.setAtmCard(card);

        // SAVE ACCOUNT (NOT CARD)
        accountRepo.save(account);


        req.setStatus(Request.APPROVED);
        req.setApprovedDate(LocalDateTime.now());
        req.setApprovedBy(current);
        atmRequestRepo.save(req);
    }

    // ================= ATM WITHDRAW =================

    @Override
    public void resetPin(Long accountId, String oldPin, String newPin) {

        if (!newPin.matches("\\d{4}"))
            throw new RuntimeException("PIN must be 4 digits");

        User current = getCurrentUser();

        AtmCard card = atmRepo.findByAccount_Id(accountId)
                .orElseThrow(() ->
                    new RuntimeException("ATM card not found"));

        if (!card.getAccount().getUser().getId()
                .equals(current.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (!passwordEncoder.matches(oldPin, card.getPinHash())) {
            throw new RuntimeException("Old PIN incorrect");
        }

        card.setPinHash(passwordEncoder.encode(newPin));
        card.setFailedPinAttempts(0);
        card.setStatus(AtmStatus.ACTIVE);

        atmRepo.save(card);

        // History
        AtmPinHistory h = new AtmPinHistory();
        h.setCardId(card.getId());
        h.setChangedAt(LocalDateTime.now());

        pinHistoryRepo.save(h);
    }


    // ================= HELPERS =================

    private void validateCard(AtmCard card, String pin) {

        if (card.getStatus() != AtmStatus.ACTIVE)
            throw new RuntimeException("ATM card blocked");

        if (!passwordEncoder.matches(pin, card.getPinHash())) {

            card.setFailedPinAttempts(
                    card.getFailedPinAttempts() + 1);

            if (card.getFailedPinAttempts() >= 3) {
                card.setStatus(AtmStatus.BLOCKED);
            }

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
    public boolean hasPendingAtmRequest(Long userId) {
        List<AtmRequest> pendingRequests = atmRequestRepo.findByRequestedBy_IdAndStatus(userId, Request.PENDING);
        return !pendingRequests.isEmpty();
    }

    @Override
    public List<AtmRequest> findByRequestedBy_IdAndStatus(Long userId, Request status) {
        return atmRequestRepo.findByRequestedBy_IdAndStatus(userId, status);
    }

    @Override
    public void rejectAtm(Long requestId, String reason) {

        if (reason == null || reason.isBlank())
            throw new RuntimeException("Rejection reason required");

        AtmRequest req = atmRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != Request.PENDING)
            throw new RuntimeException("Already processed");

        User current = getCurrentUser();
        
        // Check if current user has admin or manager role and verify branch access
        if ((current.getRole().getRoleName() == AppRole.ROLE_ADMIN || current.getRole().getRoleName() == AppRole.ROLE_MANAGER)) {
            if (!req.getAccount().getBranch().getId()
                    .equals(current.getBranch().getId())) {
                throw new RuntimeException("Unauthorized branch access");
            }
        }

        req.setStatus(Request.REJECTED);
        req.setRejectionReason(reason);
        req.setApprovedDate(LocalDateTime.now());
        req.setApprovedBy(current);

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
    
    // ================= FORGOT PIN =================
    
    @Override
    public String sendPinResetOtp(String cardNumber) {
        AtmCard card = atmRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("ATM card not found"));
        
        User user = card.getAccount().getUser();
        String email = user.getEmail();
        
        int otp = new Random().nextInt(900000) + 100000;
        OtpStore.save(cardNumber, otp);
        
        emailService.send(
            email,
            "ATM PIN Reset OTP - UnityTrust Bank",
            "Your OTP for ATM PIN reset is: " + otp + "\nValid for 5 minutes.\nCard: ****" + cardNumber.substring(cardNumber.length() - 4)
        );
        
        return email;
    }
    
    @Override
    public void resetPinWithOtp(String cardNumber, int otp, String newPin) {
        if (!OtpStore.verify(cardNumber, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        
        if (newPin == null || newPin.length() != 4) {
            throw new RuntimeException("PIN must be 4 digits");
        }
        
        AtmCard card = atmRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("ATM card not found"));
        
        card.setPinHash(passwordEncoder.encode(newPin));
        card.setFailedPinAttempts(0);
        card.setStatus(AtmStatus.ACTIVE);
        atmRepo.save(card);
        
        AtmPinHistory history = new AtmPinHistory();
        history.setCardId(card.getId());
        history.setChangedAt(LocalDateTime.now());
        pinHistoryRepo.save(history);
        
        emailService.send(
            card.getAccount().getUser().getEmail(),
            "ATM PIN Changed Successfully",
            "Your ATM PIN has been reset successfully.\nCard: ****" + cardNumber.substring(cardNumber.length() - 4) + "\nIf you did not make this change, please contact support immediately."
        );
    }
    @Override
    public AtmCard getUserAtmCard(Long accountId) {
        return atmRepo.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("ATM card not found for account: " + accountId));
    }
    @Override
    public AtmCardDetailsDto getCardDetails(Long accountId) {

        User current = getCurrentUser();

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Security: Only owner can see
        if (!account.getUser().getId().equals(current.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        AtmCard card = atmRepo.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("ATM card not found"));

        AtmCardDetailsDto dto = new AtmCardDetailsDto();

        dto.setCardNumber(card.getCardNumber());
        dto.setExpiryDate(card.getExpiryDate().toString());
        dto.setCustomerName(account.getUser()
                .getCustomerProfile()
                .getFullName());
        dto.setBranchName(account.getBranch().getBranchName());

        return dto;
    }
    
    @Override
    public List<AtmReceipt> getReceipts(Long accountId) {

        User current = getCurrentUser();

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Security check
        if (!account.getUser().getId().equals(current.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return atmReceiptRepo
                .findByAccount_IdOrderByTimeDesc(accountId);
    }
    @Override
    public AtmReceipt withdraw(String cardNumber,
                               String pin,
                               BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Invalid amount");

        AtmCard card = atmRepo.findByCardNumber(cardNumber)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Invalid ATM card"));

        validateCard(card, pin);

        // Reset daily limit
        if (!LocalDate.now().equals(card.getLastWithdrawalDate())) {
            card.setDailyWithdrawnAmount(BigDecimal.ZERO);
            card.setLastWithdrawalDate(LocalDate.now());
        }

        if (card.getDailyWithdrawnAmount()
                .add(amount)
                .compareTo(card.getDailyWithdrawalLimit()) > 0)
            throw new RuntimeException("Daily limit exceeded");

        Account account = accountRepo
                .findByIdForUpdate(card.getAccount().getId())
                .orElseThrow(() ->
                    new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        // Ledger entry
        String ref = UUID.randomUUID().toString();

        ledgerService.postEntry(
                account,
                TransactionType.DEBIT,
                amount,
                "ATM",
                "ATM Withdrawal",
                ref
        );

        // ========== CREATE RECEIPT ==========
        AtmReceipt receipt = new AtmReceipt();

        receipt.setReceiptNo(UUID.randomUUID().toString());

        receipt.setAtmId("ATM-" + card.getId()); // ✅ FIX

        receipt.setBranchCode(
                account.getBranch().getBranchCode()); // ✅ FIX

        receipt.setAmount(amount);

        receipt.setTime(LocalDateTime.now());

        receipt.setAccount(account);

        atmReceiptRepo.save(receipt);

        // Update card
        card.setDailyWithdrawnAmount(
                card.getDailyWithdrawnAmount().add(amount));

        atmRepo.save(card);

        return receipt; // 🔥 VERY IMPORTANT
    }




}