package com.example.UnityTrustBank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String password;
    private String mobile;

    private Long roleId;
    private Long branchId;
}
