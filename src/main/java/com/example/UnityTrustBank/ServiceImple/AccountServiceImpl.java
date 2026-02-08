package com.example.UnityTrustBank.ServiceImple;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.UnityTrustBank.exception.ResourceNotFoundException;
import com.example.UnityTrustBank.exception.UnauthorizedAccessException;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AccountStatus;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Enum.AtmStatus;
import com.example.UnityTrustBank.Enum.UpiStatus;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.AtmRepo;
import com.example.UnityTrustBank.Repository.UpiRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.AccountService;
import com.example.UnityTrustBank.dto.AccountResponseDto;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired private AccountRepo accountRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private UpiRepo upiRepo;
    @Autowired private AtmRepo atmRepo;
    public static final double MIN_SAVINGS_BALANCE = 0.0;
    

    @Override
    public AccountResponseDto getAccount(Long accountId) {

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        User current = getCurrentUser();

        if (!account.getUser().getId().equals(current.getId())
                && current.getRole().getRoleName() != AppRole.ROLE_ADMIN
                && current.getRole().getRoleName() != AppRole.ROLE_MANAGER) {
            throw new UnauthorizedAccessException("Access Denied: You cannot view other customers' accounts.");
        }

        return toDto(account);
    }

    @Override
    public List<AccountResponseDto> getAccountsForUser(Long userId) {

        User current = getCurrentUser();

        if (!current.getId().equals(userId)
                && current.getRole().getRoleName() != AppRole.ROLE_ADMIN
                && current.getRole().getRoleName() != AppRole.ROLE_MANAGER) {
            throw new RuntimeException("Unauthorized access");
        }

        return accountRepo.findByUser_Id(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void freezeAccount(Long accountId) {

        User admin = getCurrentAdmin();

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getBranch().getId().equals(admin.getBranch().getId())) {
            throw new RuntimeException("Unauthorized: Admin can only manage accounts in their branch.");
        }

        if (!account.getBranch().isActive()) {
            throw new RuntimeException("Branch inactive");
        }

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new RuntimeException("Account already frozen");
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new RuntimeException("Closed account cannot be frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        accountRepo.save(account);
    }


    @Override
    @Transactional
    public void unfreezeAccount(Long accountId) {

        User admin = getCurrentAdmin();

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getBranch().getId().equals(admin.getBranch().getId())) {
            throw new RuntimeException("Unauthorized: Admin can only manage accounts in their branch.");
        }

        if (!account.getBranch().isActive()) {
            throw new RuntimeException("Branch inactive");
        }

        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new RuntimeException("Only frozen accounts can be activated");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepo.save(account);
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private User getCurrentAdmin() {

        User user = getCurrentUser();

        if (user.getRole().getRoleName() != AppRole.ROLE_ADMIN && user.getRole().getRoleName() != AppRole.ROLE_MANAGER) {
            throw new RuntimeException("Admin access required");
        }
        return user;
    }

    private AccountResponseDto toDto(Account a) {
        return new AccountResponseDto(
                a.getId(),
                a.getAccountNumber(),
                a.getBalance(),
                a.getStatus()
        );
    }
       @Override
    @Transactional
    public void closeAccount(Long accountId) {

        User admin = getCurrentAdmin(); // Security Check

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getBranch().getId().equals(admin.getBranch().getId())) {
            throw new RuntimeException("Unauthorized: Admin can only close accounts in their branch.");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Withdraw full balance before closing");
        }
        upiRepo.findByAccount_Id(accountId)
                .ifPresent(upi -> upi.setStatus(UpiStatus.INACTIVE));

        atmRepo.findByAccount_Id(accountId)
                .ifPresent(card -> card.setStatus(AtmStatus.BLOCKED));

        account.setStatus(AccountStatus.CLOSED);
        accountRepo.save(account);
    }
       @Override
       public List<AccountResponseDto> getAccountsForBranch() {

           User admin = getCurrentAdmin(); // only admin/manager

           Long branchId = admin.getBranch().getId();

           return accountRepo.findByBranch_Id(branchId)
                   .stream()
                   .map(this::toDto)
                   .toList();
       }

}
