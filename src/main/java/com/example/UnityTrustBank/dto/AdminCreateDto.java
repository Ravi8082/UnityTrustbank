package com.example.UnityTrustBank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCreateDto {
    private String email;
    private String mobile;
    private String password;
    private Long branchId;
    
    // Personal details
    private String fullName;
    private String fatherName;
    private String gender; // MALE/FEMALE
    private String address;
    private String aadhaar;
    private String pan;
    
    // Document paths (will be set after file upload)
    private String profileImagePath;
    private String aadhaarImagePath;
    private String panImagePath;
}