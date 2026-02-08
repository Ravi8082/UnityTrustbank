package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.UnityTrustBank.Entity.LoanRequest;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.dto.LoanSummaryDto;
@Repository
public interface LoanRequestRepo extends JpaRepository<LoanRequest, Long> {

    List<LoanRequest> findByUser_Id(Long userId);

    List<LoanRequest> findByStatus(Request status);

    List<LoanRequest> findByStatusAndBranch_Id(Request status, Long branchId);


    // ✅ FIXED
    @Query("""
        SELECT new com.example.UnityTrustBank.dto.LoanSummaryDto(
            COALESCE(cp.fullName, u.email),
            lr.loanType,
            lr.amount,
            lr.tenureMonths,
            lr.appliedAt,
            b.branchName,
            CONCAT('', lr.status)   
        )
        FROM LoanRequest lr
        JOIN lr.user u
        LEFT JOIN u.customerProfile cp
        JOIN lr.branch b
    """)
    List<LoanSummaryDto> fetchAllLoanSummaries();
}
