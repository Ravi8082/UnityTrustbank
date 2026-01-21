package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.TransactionChannel;
import com.example.UnityTrustBank.Enum.TransactionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_txn_account", columnList = "account_id"),
        @Index(name = "idx_txn_time", columnList = "transaction_time"),
        @Index(name = "idx_txn_ref", columnList = "reference_no")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionChannel channel;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "reference_no", nullable = false, updatable = false, unique = true)
    private String referenceNo;

    @Column(length = 255)
    private String remark;

    @Column(name = "transaction_time", nullable = false, updatable = false)
    private LocalDateTime transactionTime;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @PrePersist
    protected void onCreate() {
        this.transactionTime = LocalDateTime.now();
    }
}
