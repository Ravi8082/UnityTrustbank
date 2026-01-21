package com.example.UnityTrustBank.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class BranchResponseDto {

    private Long id;
    private String branchName;
    private String branchCode;
    private String ifscCode;
    private String accountPrefix;
    private String city;
    private String state;
    private boolean active;
}
