package com.example.UnityTrustBank.ServiceImple;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.AuditLog;
import com.example.UnityTrustBank.Entity.EmiSchedule;
import com.example.UnityTrustBank.Entity.Loan;
import com.example.UnityTrustBank.Entity.LoanRequest;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AccountStatus;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Enum.EmiStatus;
import com.example.UnityTrustBank.Enum.LoanStatus;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.Enum.TransactionType;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.AuditLogRepo;
import com.example.UnityTrustBank.Repository.EmiScheduleRepo;
import com.example.UnityTrustBank.Repository.LoanRepo;
import com.example.UnityTrustBank.Repository.LoanRequestRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.LoanService;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    @Autowired 
    private LoanRequestRepo loanRequestRepo;

    @Autowired 
    private AccountRepo accountRepo;

    @Autowired 
    private UserRepo userRepo;

    @Autowired
    private LoanRepo loanRepo;

    @Autowired 
    private EmailService emailService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private EmiScheduleRepo emiRepo;
    @Autowired
    private AuditLogRepo auditLogRepo;
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public LoanRequest applyLoan(LoanRequest dto) {
        User current = getCurrentUser();

        LoanRequest lr = new LoanRequest();
        lr.setLoanType(dto.getLoanType());
        lr.setAmount(dto.getAmount());
        lr.setTenureMonths(dto.getTenureMonths());
        lr.setStatus(Request.PENDING);
        lr.setAppliedAt(LocalDateTime.now());
        lr.setUser(current);
        lr.setBranch(current.getBranch());

        LoanRequest saved = loanRequestRepo.save(lr);

        emailService.send(
                current.getEmail(),
                "Loan Application Received",
                "Dear " + current.getCustomerProfile().getFullName() + ",\n\n" +
                "Your loan request has been submitted successfully.\n" +
                "Loan Type: " + saved.getLoanType() + "\n" +
                "Amount: ₹" + saved.getAmount() + "\n" +
                "Status: PENDING\n\n" +
                "We will notify you once it is reviewed.\n\n" +
                "UnityTrust Bank"
        );

        return saved;
    }

    @Override
    public List<LoanRequest> getLoansByUser(Long userId) {
        User current = getCurrentUser();

        if (!current.getId().equals(userId) &&
            current.getRole().getRoleName() != AppRole.ROLE_ADMIN) {
            throw new RuntimeException("Unauthorized access");
        }

        return loanRequestRepo.findByUser_Id(userId);
    }

    @Override
    public List<LoanRequest> getPendingLoans() {
        User admin = getCurrentUser();

        if (admin.getRole().getRoleName() != AppRole.ROLE_ADMIN)
            throw new RuntimeException("Admin access required");

        return loanRequestRepo.findByStatusAndBranch_Id(
                Request.PENDING, admin.getBranch().getId());
    }

    @Override
    @Transactional
    public void approveLoan(Long id) {
        LoanRequest lr = loanRequestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (lr.getStatus() != Request.PENDING)
            throw new RuntimeException("Already processed");

        User admin = getCurrentUser();

        if (admin.getRole().getRoleName() != AppRole.ROLE_ADMIN)
            throw new RuntimeException("Only admin can approve loan");

        if (!lr.getBranch().getId().equals(admin.getBranch().getId()))
            throw new RuntimeException("Unauthorized branch access");

        Account account = accountRepo
                .findPrimaryAccountByUserIdForUpdate(lr.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User has no active account"));

        if (account.getStatus() != AccountStatus.ACTIVE)
            throw new RuntimeException("Account is not active");

        // Update LoanRequest status
        lr.setStatus(Request.APPROVED);
        lr.setApprovedAt(LocalDateTime.now());
        lr.setApprovedBy(admin);
        loanRequestRepo.save(lr);

        // Create Loan entity
        BigDecimal principal = lr.getAmount();
        BigDecimal interestRate = BigDecimal.valueOf(10.5);
        Loan loan = new Loan();
        loan.setPrincipal(principal);
        loan.setInterestRate(interestRate);
        loan.setTenureMonths(lr.getTenureMonths());
        loan.setEmi(BigDecimal.valueOf(calculateEmi(principal.doubleValue(), 10.5, lr.getTenureMonths())));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedAt(LocalDateTime.now());
        loan.setLoanReference("LN-" + UUID.randomUUID());
        loan.setAccount(account);
        loanRepo.save(loan);

        // Credit loan amount to user's account
        BigDecimal newBalance = ledgerService.postEntry(
                account,
                TransactionType.CREDIT,
                principal,
                "LOAN",
                "Loan disbursed",
                "LOAN-" + UUID.randomUUID()
        );

     //// EMI schedule
        BigDecimal remaining = principal;
        LocalDateTime startDate = LocalDateTime.now();

        for (int i = 0; i < lr.getTenureMonths(); i++) {
            EmiSchedule e = new EmiSchedule();
            e.setLoan(loan);

            // calculate correct due year and month
            int year = startDate.getYear() + (startDate.getMonthValue() + i - 1) / 12;
            int month = (startDate.getMonthValue() + i - 1) % 12 + 1;

            e.setDueYear(year);
            e.setDueMonth(month);

            BigDecimal monthlyPrincipal = principal
                    .divide(BigDecimal.valueOf(lr.getTenureMonths()), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal monthlyInterest = principal.multiply(BigDecimal.valueOf(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);

            e.setPrincipal(monthlyPrincipal);
            e.setInterest(monthlyInterest);
            e.setEmiAmount(loan.getEmi());
            remaining = remaining.subtract(monthlyPrincipal);
            e.setBalance(remaining.max(BigDecimal.ZERO));
            e.setStatus(EmiStatus.PENDING);

            emiRepo.save(e);
        }
        auditLogRepo.save(new AuditLog(
        	    null,
        	    "LOAN_APPROVED",
        	    admin.getId(),
        	    LocalDateTime.now()
        	));




        // Send email
        emailService.send(
                lr.getUser().getEmail(),
                "UnityTrust Bank – Loan Disbursed",
                "Dear " + lr.getUser().getCustomerProfile().getFullName() + ",\n\n" +
                "Your loan has been APPROVED and DISBURSED.\n\n" +
                "Loan Amount Credited: ₹" + lr.getAmount() + "\n" +
                "Tenure: " + lr.getTenureMonths() + " months\n" +
                "New Account Balance: ₹" + newBalance + "\n\n" +
                "Loan Reference: " + loan.getLoanReference() + "\n\n" +
                "Aadhar Number: "+ lr.getUser().getCustomerProfile().getAadhaar() + "\n" +
                "Pan Number: "+ lr.getUser().getCustomerProfile().getPan() + "\n\n" +
                "Regards,\nUnityTrust Bank"
        );
    }

    @Override
    public void rejectLoan(Long id, String reason) {
        if (reason == null || reason.isBlank())
            throw new RuntimeException("Reason required");

        LoanRequest lr = loanRequestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (lr.getStatus() != Request.PENDING)
            throw new RuntimeException("Already processed");

        lr.setStatus(Request.REJECTED);
        lr.setApprovedAt(LocalDateTime.now()); // reused for rejection timestamp
        lr.setApprovedBy(getCurrentUser());    // reused for rejectedBy
        loanRequestRepo.save(lr);

        emailService.send(
                lr.getUser().getEmail(),
                "Loan Rejected",
                "Dear " + lr.getUser().getCustomerProfile().getFullName() + ",\n\n" +
                "We regret to inform you that your loan request has been REJECTED.\n" +
                "Reason: " + reason + "\n\n" +
                "You may reapply after addressing the issue.\n\n" +
                "UnityTrust Bank"
        );
    }


    private Double calculateEmi(Double principal, Double rate, Integer months) {
        double r = rate / (12 * 100);
        return (principal * r * Math.pow(1 + r, months)) /
               (Math.pow(1 + r, months) - 1);
    }
}
