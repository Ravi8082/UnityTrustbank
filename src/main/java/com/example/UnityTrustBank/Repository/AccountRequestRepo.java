package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AccountRequest;
import com.example.UnityTrustBank.Enum.Request;

public interface AccountRequestRepo  extends JpaRepository<AccountRequest, Long>{

	boolean existsByUser_IdAndStatus(Long userid, Request pending);

	List<AccountRequest> findByBranch_IdAndStatus(Long branchId, Request pending);

	List<AccountRequest> findByUser_Id(Long userId);

}
