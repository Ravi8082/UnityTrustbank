package com.example.UnityTrustBank.ServiceImple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.Transaction;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AccountStatus;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.TransactionRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.TransactionService;
import com.example.UnityTrustBank.dto.TransactionResponseDto;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired private TransactionRepo txnRepo;
    @Autowired private AccountRepo accountRepo;
    @Autowired private UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getStatement(Long accountId) {

        Account account = validateAccountAccess(accountId);

        return txnRepo
                .findByAccount_IdOrderByTransactionTimeDesc(account.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getMiniStatement(Long accountId) {

        Account account = validateAccountAccess(accountId);

        return txnRepo
                .findTop10ByAccount_IdOrderByTransactionTimeDesc(account.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Account validateAccountAccess(Long accountId) {

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new RuntimeException("Account closed");
        }

        User current = getCurrentUser();

        if (current.getRole().getRoleName() == AppRole.ROLE_USER) {

            if (!account.getUser().getId().equals(current.getId())) {
                throw new RuntimeException("Unauthorized account access");
            }
        }


        if (current.getRole().getRoleName() == AppRole.ROLE_ADMIN) {

            if (!account.getBranch().getId()
                    .equals(current.getBranch().getId())) {
                throw new RuntimeException("Unauthorized branch access");
            }
        }

        return account;
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

 
    private TransactionResponseDto toDto(Transaction t) {

        return new TransactionResponseDto(
                t.getId(),
                t.getType(),
                t.getChannel(),
                t.getAmount(),
                t.getBalanceAfter(),
                t.getReferenceNo(),
                t.getRemark(),
                t.getTransactionTime()
        );
    }
}
