package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.TransactionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "ledger_entries",
    indexes = {
        @Index(name = "idx_ledger_account", columnList = "account_id"),
        @Index(name = "idx_ledger_reference", columnList = "reference_no"),
        @Index(name = "idx_ledger_created", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔒 Always lazy for performance
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;   // DEBIT / CREDIT

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "running_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal runningBalance;

    @Column(name = "reference_no", nullable = false, updatable = false, length = 50)
    private String referenceNo;

    @Column(nullable = false, length = 20)
    private String channel;   // ATM / UPI / NET_BANKING

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
