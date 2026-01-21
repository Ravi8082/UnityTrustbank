package com.example.UnityTrustBank.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountApplicationCreateDto {

    private String fullName;
    private String fatherName;
    private String email;
    private String mobile;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;

    private String aadhaar;
    private String pan;
    private String address;
    private String accountType;
    private Long branchId;
}
