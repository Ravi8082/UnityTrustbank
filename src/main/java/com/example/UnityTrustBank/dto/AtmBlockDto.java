package com.example.UnityTrustBank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtmBlockDto {

    private String cardNumber;
    private String reason;
}
