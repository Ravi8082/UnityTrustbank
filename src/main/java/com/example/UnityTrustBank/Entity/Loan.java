package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.LoanStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "loans",
    indexes = {
        @Index(name = "idx_loan_account", columnList = "account_id"),
        @Index(name = "idx_loan_status", columnList = "status"),
        @Index(name = "idx_loan_ref", columnList = "loan_reference")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_loan_reference", columnNames = "loan_reference")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💰 Financial fields (BigDecimal = SAFE)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate; // e.g. 10.50 %

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private LoanStatus status;   // APPROVED, ACTIVE, CLOSED

    @Column(name = "disbursed_at")
    private LocalDateTime disbursedAt;

    @Column(name = "loan_reference", nullable = false, updatable = false, length = 50)
    private String loanReference;

    // 🔗 Relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "loans", "transactions", "atmCard"})
    private Account account;
}
