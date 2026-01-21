package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.EmiStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "emi_schedule",
    indexes = {
        @Index(name = "idx_emi_loan", columnList = "loan_id"),
        @Index(name = "idx_emi_due_month", columnList = "due_year, due_month"),
        @Index(name = "idx_emi_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "due_year", nullable = false)
    private Integer dueYear;

    @Column(name = "due_month", nullable = false)
    private Integer dueMonth; 

    @Column(name = "principal", nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(name = "interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal interest;

    @Column(name = "emi_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EmiStatus status;  // PAID, FAILED, PENDING

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount = 0;
}
