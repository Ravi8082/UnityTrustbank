package com.example.UnityTrustBank.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.ApplicationStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "account_applications",
    indexes = {
        @Index(name = "idx_app_branch", columnList = "branch_id"),
        @Index(name = "idx_app_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String fatherName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String mobile;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(length = 12, nullable = false)
    private String aadhaar;

    @Column(length = 10, nullable = false)
    private String pan;

    private String address;
    private String accountType;

    private String profileImagePath;
    private String aadhaarImagePath;
    private String panImagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private String rejectionReason;

    private LocalDateTime appliedAt;
    private LocalDateTime decisionAt;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;
}
