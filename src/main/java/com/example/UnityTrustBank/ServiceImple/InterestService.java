package com.example.UnityTrustBank.ServiceImple;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.Transaction;
import com.example.UnityTrustBank.Enum.AccountStatus;
import com.example.UnityTrustBank.Enum.TransactionChannel;
import com.example.UnityTrustBank.Enum.TransactionType;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.TransactionRepo;

@Service
public class InterestService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private LedgerService ledgerService;

    // Savings account interest rate: 4% per annum
    private static final BigDecimal SAVINGS_INTEREST_RATE = new BigDecimal("0.04");
    
    // Current account interest rate: 0% (no interest)
    private static final BigDecimal CURRENT_INTEREST_RATE = BigDecimal.ZERO;

    /**
     * Runs on 1st of every month at 2:00 AM
     * Cron: second minute hour day month day-of-week
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void calculateAndCreditMonthlyInterest() {
        System.out.println("Starting monthly interest calculation at: " + LocalDateTime.now());

        List<Account> activeAccounts = accountRepo.findByStatus(AccountStatus.ACTIVE);

        int successCount = 0;
        int failCount = 0;

        for (Account account : activeAccounts) {
            try {
                BigDecimal interestAmount = calculateInterest(account);

                if (interestAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Credit interest to account
                    account.setBalance(account.getBalance().add(interestAmount));
                    accountRepo.save(account);

                    // Create transaction record
                    Transaction txn = new Transaction();
                    txn.setAccount(account);
                    txn.setAmount(interestAmount);
                    txn.setType(TransactionType.CREDIT);
                    txn.setChannel(TransactionChannel.INTERNAL);
                    txn.setDescription("Monthly interest credited");
                    txn.setTimestamp(LocalDateTime.now());
                    transactionRepo.save(txn);

                    // Create ledger entry
                    ledgerService.recordTransaction(
                            txn.getId(),
                            account.getId(),
                            "INTEREST_INCOME",
                            interestAmount,
                            "Monthly interest for account " + account.getAccountNumber()
                    );

                    successCount++;
                }
            } catch (Exception e) {
                System.err.println("Error calculating interest for account " 
                        + account.getAccountNumber() + ": " + e.getMessage());
                failCount++;
            }
        }

        System.out.println("Interest calculation completed. Success: " 
                + successCount + ", Failed: " + failCount);
    }

    private BigDecimal calculateInterest(Account account) {
        BigDecimal rate = account.getAccountType().equals("SAVINGS") 
                ? SAVINGS_INTEREST_RATE 
                : CURRENT_INTEREST_RATE;

        // Monthly interest = (Principal × Annual Rate) / 12
        BigDecimal monthlyRate = rate.divide(new BigDecimal("12"), 6, BigDecimal.ROUND_HALF_UP);
        BigDecimal interest = account.getBalance().multiply(monthlyRate);

        // Round to 2 decimal places
        return interest.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Manual trigger for testing (can be called via admin endpoint)
     */
    public void triggerInterestCalculation() {
        calculateAndCreditMonthlyInterest();
    }
}
