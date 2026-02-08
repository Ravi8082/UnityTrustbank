package com.example.UnityTrustBank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtmPinResetDto {

    private Long accountId;   // instead of cardId
    private String oldPin;
    private String newPin;
}
