package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.Loan;
import com.example.UnityTrustBank.Entity.LoanRequest;
import com.example.UnityTrustBank.Enum.Request;

public interface LoanRepo extends JpaRepository<Loan, Long> {
	List<Loan> findByStatus(String status);
	List<LoanRequest> findByStatus(Request status);

}
