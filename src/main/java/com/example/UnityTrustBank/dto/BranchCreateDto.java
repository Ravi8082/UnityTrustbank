package com.example.UnityTrustBank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchCreateDto {

    private String branchName;
    private String branchCode;
    private String ifscCode;
    private String accountPrefix;
    private String city;
    private String state;
}
