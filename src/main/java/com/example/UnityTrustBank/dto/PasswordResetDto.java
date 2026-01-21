package com.example.UnityTrustBank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
