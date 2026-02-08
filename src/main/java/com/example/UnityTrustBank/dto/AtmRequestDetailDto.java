// Create new DTO file: AtmRequestDetailDto.java
package com.example.UnityTrustBank.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AtmRequestDetailDto {
    private Long id;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
    private String rejectionReason;
    private String customerName;
    private String email;
    private String accountNumber;
    private String branchName;
    private String branchCode;
}