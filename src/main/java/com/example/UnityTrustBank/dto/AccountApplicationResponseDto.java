package com.example.UnityTrustBank.dto;

import java.time.LocalDateTime;
import com.example.UnityTrustBank.Enum.ApplicationStatus;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountApplicationResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    private String aadhaar;
    private String pan;
    private String address;
    private String accountType;
    private String profileImagePath;
    private String aadhaarImagePath;
    private String panImagePath;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime decisionAt;
}