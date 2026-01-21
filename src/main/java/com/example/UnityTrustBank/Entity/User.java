package com.example.UnityTrustBank.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_user_mobile", columnNames = "mobile")
    },
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JWT SUBJECT — NEVER UPDATE
    @Column(nullable = false, length = 100, updatable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean passwordResetRequired;


    @Column(length = 15)
    private String mobile;

    private boolean active;

    // OPTIMISTIC LOCK
    @Version
    private Long version;

    // ROLE
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    // BRANCH
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    // KYC (optional at creation)
    @OneToOne(
        mappedBy = "user",
        cascade = CascadeType.PERSIST,
        fetch = FetchType.LAZY
    )
    private CustomerProfile customerProfile;
}
