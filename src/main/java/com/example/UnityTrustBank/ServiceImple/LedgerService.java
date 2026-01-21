package com.example.UnityTrustBank.ServiceImple;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.LedgerEntry;
import com.example.UnityTrustBank.Enum.TransactionType;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.LedgerRepo;

@Service
@Transactional
public class LedgerService {

    @Autowired
    private LedgerRepo ledgerRepo;

    @Autowired
    private AccountRepo accountRepo;

    public BigDecimal postEntry(
            Account account,
            TransactionType type,
            BigDecimal amount,
            String channel,
            String remark,
            String referenceNo
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid transaction amount");
        }

        // Lock account row (FOR UPDATE)
        Account lockedAccount = accountRepo.findByIdForUpdate(account.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        BigDecimal currentBalance = lockedAccount.getBalance();
        BigDecimal newBalance;

        if (type == TransactionType.CREDIT) {
            newBalance = currentBalance.add(amount);
        } else {
            newBalance = currentBalance.subtract(amount);
        }

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Ledger Entry
        LedgerEntry entry = new LedgerEntry();
        entry.setAccount(lockedAccount);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setRunningBalance(newBalance);
        entry.setReferenceNo(referenceNo);
        entry.setChannel(channel);
        entry.setRemark(remark);
        entry.setCreatedAt(LocalDateTime.now());

        ledgerRepo.save(entry);

        // Update Account Balance
        lockedAccount.setBalance(newBalance);
        accountRepo.save(lockedAccount);

        return newBalance;
    }
}
