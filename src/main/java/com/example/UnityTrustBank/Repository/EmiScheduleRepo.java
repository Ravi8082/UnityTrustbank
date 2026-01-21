package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.EmiSchedule;

public interface EmiScheduleRepo extends JpaRepository<EmiSchedule, Long> {

    // Find all EMIs for a loan
    List<EmiSchedule> findByLoan_Id(Long loanId);

    // Find EMIs by loan and due month
    List<EmiSchedule> findByLoan_IdAndDueMonth(Long loanId, int dueMonth);

    // Find EMIs by loan, due year, and due month
    List<EmiSchedule> findByLoan_IdAndDueYearAndDueMonth(Long loanId, int dueYear, int dueMonth);
}
