package com.example.UnityTrustBank.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import com.example.UnityTrustBank.Enum.AtmStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "atm_cards",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_atm_card_number", columnNames = "card_number")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, updatable = false, length = 16)
    private String cardNumber;

    @Column(nullable = false)
    private String pinHash;

    @Column(nullable = false)
    private String cvvHash;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AtmStatus status;

    @Column(nullable = false)
    private int failedPinAttempts;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyWithdrawalLimit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyWithdrawnAmount;

    private LocalDate lastWithdrawalDate;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Version
    private Long version;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;
}
