package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.dto.AccountResponseDto;

public interface AccountService {

    AccountResponseDto getAccount(Long accountId);

    List<AccountResponseDto> getAccountsForUser(Long userId);

    void freezeAccount(Long accountId);

    void unfreezeAccount(Long accountId);
    
    void closeAccount(Long accountId);
    List<AccountResponseDto> getAccountsForBranch();

}
