package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.Transaction;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_IdOrderByTransactionTimeDesc(Long accountId);

    List<Transaction> findTop10ByAccount_IdOrderByTransactionTimeDesc(Long accountId);
}
