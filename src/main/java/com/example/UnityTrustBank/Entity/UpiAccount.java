package com.example.UnityTrustBank.Entity;

import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.UpiStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "upi_accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_upi_vpa", columnNames = "vpa")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpiAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // example: ravi@utb
    @Column(nullable = false, updatable = false)
    private String vpa;

    @Column(nullable = false)
    private String pinHash;

    @Enumerated(EnumType.STRING)
    private UpiStatus status;

    private LocalDateTime createdAt;

    @Version
    private Long version;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "account_id",
        nullable = false,
        unique = true
    )
    private Account account;
}
