package com.example.UnityTrustBank.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDto {

    private Long userId;
    private String email;
    private String mobile;
    private String role;
    private boolean active;

    private String fullName;
    private String address;

    private String branchName;

    private List<String> accountNumbers;
}
