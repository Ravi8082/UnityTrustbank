package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AtmPinHistory;

public interface AtmPinHistoryRepo extends JpaRepository<AtmPinHistory, Long> {

    // Fetch PIN change history for a specific ATM card
    List<AtmPinHistory> findByCardIdOrderByChangedAtDesc(Long cardId);
}
