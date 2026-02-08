package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.UnityTrustBank.Entity.LoanDisbursement;
import com.example.UnityTrustBank.dto.LoanSummaryDto;
@Repository
public interface LoanDisbursementRepo
        extends JpaRepository<LoanDisbursement, Long> {

    @Query("""
        SELECT new com.example.UnityTrustBank.dto.LoanSummaryDto(
            COALESCE(cp.fullName, d.customerName),
            'HOME',
            d.amount,
            d.tenureMonths,
            d.disbursedAt,
            d.branchName,
            CONCAT('', l.status)  
        )
        FROM LoanDisbursement d
        JOIN d.loan l
        JOIN l.account a
        JOIN a.user u
        LEFT JOIN u.customerProfile cp
        WHERE u.id = :userId
    """)
    List<LoanSummaryDto> findUserLoanSummary(@Param("userId") Long userId);


    @Query("""
        SELECT new com.example.UnityTrustBank.dto.LoanSummaryDto(
            COALESCE(cp.fullName, d.customerName),
            'HOME',
            d.amount,
            d.tenureMonths,
            d.disbursedAt,
            d.branchName,
            CONCAT('', l.status)   
        )
        FROM LoanDisbursement d
        JOIN d.loan l
        JOIN l.account a
        JOIN a.user u
        LEFT JOIN u.customerProfile cp
    """)
    List<LoanSummaryDto> findAllSummaries();
}
