package com.example.UnityTrustBank.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AuditLog;

public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {}
