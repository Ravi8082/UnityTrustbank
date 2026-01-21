package com.example.UnityTrustBank.dto;

import java.math.BigDecimal;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpiPayDto {

    private String fromVpa;
    private String toVpa;
    private String pin;
    private BigDecimal amount;
    private String remark;
}
