package com.example.UnityTrustBank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpiCreateDto {

    private Long accountId;
    private String vpa;
    private String pin;
}
