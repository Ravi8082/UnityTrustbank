package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AtmReceipt;

public interface AtmReceiptRepo extends JpaRepository<AtmReceipt, Long> {
	List<AtmReceipt> findByAccount_IdOrderByTimeDesc(Long accountId);
}
