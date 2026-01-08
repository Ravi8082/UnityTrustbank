package com.example.UnityTrustBank.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CustomerProfileDto {

    private String fullName;
    private String fatherName;
    private String address;
    private LocalDate dob;
}
