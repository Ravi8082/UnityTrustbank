package com.example.UnityTrustBank.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.UnityTrustBank.Entity.AccountApplication;
import com.example.UnityTrustBank.Enum.ApplicationStatus;

import jakarta.persistence.LockModeType;

public interface AccountApplicationRepo
        extends JpaRepository<AccountApplication, Long> {

    // ================= LOCK =================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountApplication a WHERE a.id = :id")
    Optional<AccountApplication> findByIdForUpdate(@Param("id") Long id);


    // ================= EXISTS =================

    boolean existsByEmailAndStatus(String email, ApplicationStatus status);

    boolean existsByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByAadhaar(String aadhaar);

    boolean existsByPan(String pan);

    boolean existsByAadhaarAndStatusIn(
            String aadhaar,
            List<ApplicationStatus> statuses
    );

    boolean existsByMobileAndStatusIn(
            String mobile,
            List<ApplicationStatus> statuses
    );


    List<AccountApplication> findByBranch_IdAndStatus(
            Long branchId,
            ApplicationStatus status
    );

    List<AccountApplication> findByBranch_IdOrderByAppliedAtDesc(
            Long branchId
    );


    long countByBranch_IdAndStatus(
            Long branchId,
            ApplicationStatus status
    );

    long countByStatus(ApplicationStatus status);
}
