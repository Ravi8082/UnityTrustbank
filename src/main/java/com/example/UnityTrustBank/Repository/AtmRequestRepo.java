package com.example.UnityTrustBank.Repository;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AtmRequest;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.Request;

public interface AtmRequestRepo extends JpaRepository<AtmRequest, Long> {

	
	List<AtmRequest> findByStatus(Request status);
	Long countByStatus(Request pending);
	
	Optional<User> findByAccount_Branch_IdAndStatus(Long branchId, Request pending);
	
	List<AtmRequest> findByRequestedBy_IdAndStatus(Long userId, Request status);

}