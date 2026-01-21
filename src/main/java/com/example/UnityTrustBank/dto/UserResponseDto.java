package com.example.UnityTrustBank.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String email;
    private String mobile;
    private boolean active;
    private String role;
}
