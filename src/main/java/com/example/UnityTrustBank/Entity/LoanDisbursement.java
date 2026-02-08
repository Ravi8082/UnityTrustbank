package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_disbursements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDisbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loanReference;

    private BigDecimal amount;

    private Integer tenureMonths;

    private BigDecimal emi;

    private LocalDateTime disbursedAt;

    private String approvedBy;

    private String customerName;

    private String branchName;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;


}
