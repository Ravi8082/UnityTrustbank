package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.dto.TransactionResponseDto;

public interface TransactionService {

    List<TransactionResponseDto> getStatement(Long accountId);

    List<TransactionResponseDto> getMiniStatement(Long accountId);
}
