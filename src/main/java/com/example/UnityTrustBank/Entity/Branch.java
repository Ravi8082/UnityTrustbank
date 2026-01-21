package com.example.UnityTrustBank.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "branches",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_code", columnNames = "branchCode"),
        @UniqueConstraint(name = "uk_branch_ifsc", columnNames = "ifscCode"),
        @UniqueConstraint(name = "uk_branch_prefix", columnNames = "accountPrefix")
    },
    indexes = {
        @Index(name = "idx_branch_city", columnList = "city"),
        @Index(name = "idx_branch_active", columnList = "active")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // IMMUTABLE IDENTIFIERS
    @Column(nullable = false, updatable = false)
    private String branchName;

    @Column(nullable = false, updatable = false)
    private String branchCode;

    @Column(nullable = false, updatable = false)
    private String ifscCode;

    @Column(nullable = false, updatable = false)
    private String accountPrefix;

    private String city;
    private String state;

    private boolean active;

    // optimistic locking
    @Version
    private Long version;
}
