package com.example.UnityTrustBank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtmCardDto {
    private Long id;
    private String cardNumber;  // Masked version like "4532XXXXXX1234"
    private String accountNumber;
    private String status;
    private LocalDate expiryDate;
    private LocalDateTime issuedAt;
    private Integer dailyWithdrawalLimit;
    private BigDecimal dailyWithdrawnAmount;
    private LocalDateTime lastWithdrawalDate;
    
    // constructors, getters, setters
}