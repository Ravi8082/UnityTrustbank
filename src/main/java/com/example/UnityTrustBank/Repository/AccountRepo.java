package com.example.UnityTrustBank.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Enum.AccountStatus;

import jakarta.persistence.LockModeType;

public interface AccountRepo extends JpaRepository<Account, Long> {

    List<Account> findByUser_Id(Long userId);

    List<Account> findByBranch_Id(Long branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

	boolean existsByUser_Id(Long id);
	Optional<Account> findByAccountNumber(String accountNumber);

	boolean existsByBranch_IdAndStatus(Long branchId, AccountStatus status);
	long countByBranch_IdAndStatus(Long branchId, AccountStatus status);
	long countByBranch_Id(Long branchId);
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
	Optional<Account> findPrimaryAccountByUserIdForUpdate(@Param("userId") Long userId);



}
