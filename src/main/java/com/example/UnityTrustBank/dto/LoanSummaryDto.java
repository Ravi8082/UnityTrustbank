package com.example.UnityTrustBank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanSummaryDto {

    private String applicantName;
    private String loanType;
    private BigDecimal amount;
    private Integer tenure;
    private LocalDateTime appliedDate;
    private String branch;
    private String status;   
}
