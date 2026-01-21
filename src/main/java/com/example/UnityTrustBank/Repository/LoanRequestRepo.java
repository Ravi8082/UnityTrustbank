package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.LoanRequest;
import com.example.UnityTrustBank.Enum.Request;

public interface LoanRequestRepo extends JpaRepository<LoanRequest, Long> {

    List<LoanRequest> findByUser_Id(Long userId);

    List<LoanRequest> findByStatusAndBranch_Id(Request status, Long branchId);
}
