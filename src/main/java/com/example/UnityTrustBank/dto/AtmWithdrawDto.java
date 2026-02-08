package com.example.UnityTrustBank.dto;

import java.math.BigDecimal;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmWithdrawDto {

    private String cardNumber;   // 👈 instead of cardId
    private String pin;
    private BigDecimal amount;
}
