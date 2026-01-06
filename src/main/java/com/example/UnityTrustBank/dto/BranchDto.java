package com.example.UnityTrustBank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchDto {

    private Long id;
    private String branchName;
    private String branchCode;
    private String ifscCode;
    private String accountPrefix;
    private String city;
    private String state;
    private Boolean active;
}
