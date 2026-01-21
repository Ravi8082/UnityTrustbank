package com.example.UnityTrustBank.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileCreateDto {

    private String fullName;
    private String fatherName;
    private String address;
    private LocalDate dob;
    private String profileImage;
    private String aadhaarImage;
    private String panImage;

    
}
