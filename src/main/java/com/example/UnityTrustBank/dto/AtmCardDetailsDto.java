package com.example.UnityTrustBank.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AtmCardDetailsDto {

    private String cardNumber;
    private String expiryDate;
    private String customerName;
    private String branchName;
}
