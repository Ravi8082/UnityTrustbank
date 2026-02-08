package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.Entity.LoanRequest;
import com.example.UnityTrustBank.dto.LoanApplyDto;
import com.example.UnityTrustBank.dto.LoanSummaryDto;
public interface LoanService {

    LoanRequest applyLoan(LoanApplyDto dto);

    List<LoanRequest> getLoansByUser(Long userId);

    List<LoanRequest> getPendingLoans();

    void approveLoan(Long id);

    void rejectLoan(Long id, String reason);

    List<LoanSummaryDto> getUserLoanSummary(Long userId);

    List<LoanSummaryDto> getAllLoanApplications();
}
