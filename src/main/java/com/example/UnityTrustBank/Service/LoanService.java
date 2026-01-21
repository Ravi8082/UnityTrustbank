package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.Entity.LoanRequest;

public interface LoanService {
    LoanRequest applyLoan(LoanRequest dto);
    List<LoanRequest> getLoansByUser(Long userId);
    List<LoanRequest> getPendingLoans();
    void approveLoan(Long id);
    void rejectLoan(Long id, String reason);
}
