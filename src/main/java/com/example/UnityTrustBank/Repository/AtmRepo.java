package com.example.UnityTrustBank.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.AtmCard;
import com.example.UnityTrustBank.Enum.Request;

public interface AtmRepo extends JpaRepository<AtmCard, Long> {

    Optional<AtmCard> findByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);

	boolean existsByCardNumber(String card);

	boolean existsByAccount_IdAndStatus(Long accountId, Request pending);
	long countByAccount_Branch_IdAndStatus(Long branchId, Request status);

	Optional<AtmCard> findByCardNumber(String cardNumber);


}
