package com.example.UnityTrustBank.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.UpiTransaction;

public interface UpiTransactionRepo extends JpaRepository<UpiTransaction, Long> {}
