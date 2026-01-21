package com.example.UnityTrustBank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.TransactionChannel;
import com.example.UnityTrustBank.Enum.TransactionType;

import lombok.*;

@Data
@AllArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private TransactionType type;
    private TransactionChannel channel;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceNo;
    private String remark;
    private LocalDateTime transactionTime;
}
