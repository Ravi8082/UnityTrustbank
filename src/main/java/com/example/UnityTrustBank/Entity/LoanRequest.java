package com.example.UnityTrustBank.Entity;

import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.Request;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loanType;
    private Double amount;
    private Integer tenureMonths;

    @Enumerated(EnumType.STRING)
    private Request status;

    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;

    @ManyToOne
    private User user;

    @ManyToOne
    private Branch branch;


    @ManyToOne
    private User approvedBy;
}
