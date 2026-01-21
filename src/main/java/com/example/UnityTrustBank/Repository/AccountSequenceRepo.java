package com.example.UnityTrustBank.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.UnityTrustBank.Entity.AccountSequence;
import com.example.UnityTrustBank.Entity.Branch;

import jakarta.persistence.LockModeType;

public interface AccountSequenceRepo
        extends JpaRepository<AccountSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AccountSequence s where s.branch.id = :branchId")
    Optional<AccountSequence> findByBranchForUpdate(
            @Param("branchId") Long branchId);

	Optional<AccountSequence> findByBranch_Id(Long branchId);
}
