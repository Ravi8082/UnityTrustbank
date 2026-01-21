package com.example.UnityTrustBank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtmPinResetDto {
    private Long cardId;
    private String oldPin;
    private String newPin;
}
