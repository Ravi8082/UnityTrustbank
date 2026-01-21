package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.UnityTrustBank.Entity.LedgerEntry;

public interface LedgerRepo extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByAccount_IdOrderByCreatedAtDesc(Long accountId);

    @Query("SELECT l FROM LedgerEntry l WHERE l.referenceNo = :ref")
    List<LedgerEntry> findByReferenceNo(@Param("ref") String referenceNo);

    @Query("SELECT l FROM LedgerEntry l WHERE l.account.id = :accountId ORDER BY l.createdAt DESC")
    List<LedgerEntry> getLedgerForAccount(@Param("accountId") Long accountId);
    @Query("select l from LedgerEntry l where l.account.id = :accountId order by l.createdAt desc")
    List<LedgerEntry> findByAccount(@Param("accountId") Long accountId);
}
