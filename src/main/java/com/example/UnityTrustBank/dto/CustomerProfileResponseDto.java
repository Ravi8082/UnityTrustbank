package com.example.UnityTrustBank.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@AllArgsConstructor
public class CustomerProfileResponseDto {

    private Long id;
    private String fullName;
    private String fatherName;
    private String address;
    private LocalDate dob;

    private String aadhaar;
    private String pan;

    private boolean aadhaarVerified;
    private boolean panVerified;
    private boolean branchVisitRequired;
}
