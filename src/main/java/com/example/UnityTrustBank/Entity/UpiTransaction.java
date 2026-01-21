package com.example.UnityTrustBank.Entity;



import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.UpiTxnStatus;

import com.example.UnityTrustBank.Enum.UpiTxnStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpiTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referenceNo;

    private String fromVpa;
    private String toVpa;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private UpiTxnStatus status;

    private LocalDateTime createdAt;
}
