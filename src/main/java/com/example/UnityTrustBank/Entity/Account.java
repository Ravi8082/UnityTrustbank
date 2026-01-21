package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.AccountStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_number", columnNames = "account_number")
    },
    indexes = {
        @Index(name = "idx_account_user", columnList = "user_id"),
        @Index(name = "idx_account_branch", columnList = "branch_id"),
        @Index(name = "idx_account_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, updatable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime openedAt;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @PrePersist
    protected void onCreate() {
        this.openedAt = LocalDateTime.now();
    }
}
