package com.example.UnityTrustBank.Repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.UpiUsage;

public interface UpiUsageRepo extends JpaRepository<UpiUsage, Long> {

    Optional<UpiUsage> findByUserIdAndDate(Long userId, LocalDate date);
}
