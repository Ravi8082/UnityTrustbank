package com.example.UnityTrustBank.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AtmReceipt;

public interface AtmReceiptRepo extends JpaRepository<AtmReceipt, Long> {}
