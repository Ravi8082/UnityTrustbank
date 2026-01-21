package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AccountApplication;
import com.example.UnityTrustBank.Enum.ApplicationStatus;

public interface AccountApplicationRepo
        extends JpaRepository<AccountApplication, Long> {

    boolean existsByEmailAndStatus(String email,ApplicationStatus status);

    List<AccountApplication> findByBranch_IdAndStatus(Long branchId, ApplicationStatus status );

    boolean existsByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByAadhaar(String aadhaar);

	boolean existsByPan(String pan);
	long countByBranch_IdAndStatus(Long branchId, ApplicationStatus status);

}
