package com.example.UnityTrustBank.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplyDto {
   private String loanType;
   private BigDecimal amount;
   private Integer tenure;
}
