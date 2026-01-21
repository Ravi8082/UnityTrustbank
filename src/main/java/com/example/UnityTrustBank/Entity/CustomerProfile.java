package com.example.UnityTrustBank.Entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "customer_profiles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cp_aadhaar", columnNames = "aadhaar"),
        @UniqueConstraint(name = "uk_cp_pan", columnNames = "pan")
    },
    indexes = {
        @Index(name = "idx_cp_user", columnList = "user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String fatherName;
    private String address;
    private LocalDate dob;
    @Column(name = "profile_image_path")
    private String profileImagePath;
    @Column(name = "aadhaar_image_path")
    private String aadhaarImagePath;

    @Column(name = "pan_image_path")
    private String panImagePath;


    @Column(length = 12)
    private String aadhaar;

    @Column(length = 10)
    private String pan;

    private boolean aadhaarVerified;
    private boolean panVerified;

    private boolean branchVisitRequired;

    @Version
    private Long version;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;
}
