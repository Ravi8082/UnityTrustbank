package com.example.UnityTrustBank.dto;

import java.time.LocalDateTime;
import com.example.UnityTrustBank.Enum.ApplicationStatus;
import lombok.*;

@Data
@AllArgsConstructor
public class AccountApplicationResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime decisionAt;
}
