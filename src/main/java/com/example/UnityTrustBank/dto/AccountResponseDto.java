package com.example.UnityTrustBank.dto;

import java.math.BigDecimal;

import com.example.UnityTrustBank.Enum.AccountStatus;
import lombok.*;

@Data
@AllArgsConstructor
public class AccountResponseDto {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
}
