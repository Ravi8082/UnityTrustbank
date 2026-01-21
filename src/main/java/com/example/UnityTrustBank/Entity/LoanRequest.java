package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.Request;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    private BigDecimal amount;
    private Integer tenureMonths;

    @Enumerated(EnumType.STRING)
    private Request status;

    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;

    @ManyToOne
    @JsonIgnoreProperties({
        "loanRequests", "accounts", "customerProfile", "branch"
    })
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({
        "users", "accounts", "accountSequence"
    })
    private Branch branch;

    @ManyToOne
    @JsonIgnoreProperties({
        "loanRequests", "accounts", "customerProfile", "branch"
    })
    private User approvedBy;
}
