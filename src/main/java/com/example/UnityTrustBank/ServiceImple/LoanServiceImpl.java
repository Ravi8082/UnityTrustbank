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
import com.example.UnityTrustBank.Entity.LoanDisbursement;
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
import com.example.UnityTrustBank.Repository.LoanDisbursementRepo;
import com.example.UnityTrustBank.Repository.LoanRepo;
import com.example.UnityTrustBank.Repository.LoanRequestRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.LoanService;
import com.example.UnityTrustBank.dto.LoanApplyDto;
import com.example.UnityTrustBank.dto.LoanSummaryDto;
import com.example.UnityTrustBank.exception.ResourceNotFoundException;
import com.example.UnityTrustBank.exception.UnauthorizedAccessException;
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
    private LoanDisbursementRepo disbursementRepo;

    @Autowired
    private EmiScheduleRepo emiRepo;

    @Autowired
    private AuditLogRepo auditLogRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LedgerService ledgerService;


    // ================= CURRENT USER =================

    private User getCurrentUser() {
        String email =
            SecurityContextHolder.getContext()
                                 .getAuthentication()
                                 .getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException("User not found"));
    }


    // ================= APPLY LOAN =================

    @Override
    public LoanRequest applyLoan(LoanApplyDto dto) {

        User user = getCurrentUser();

        if (dto.getTenure() == null || dto.getTenure() <= 0) {
            throw new RuntimeException("Invalid tenure");
        }

        if (dto.getAmount() == null ||
            dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException("Invalid amount");
        }

        LoanRequest lr = new LoanRequest();

        lr.setLoanType(dto.getLoanType());
        lr.setAmount(dto.getAmount());

        // ✅ THIS FIXES N/A
        lr.setTenureMonths(dto.getTenure());

        lr.setStatus(Request.PENDING);
        lr.setAppliedAt(LocalDateTime.now());

        lr.setUser(user);

        if (user.getBranch() == null) {
            throw new RuntimeException("User has no branch");
        }

        lr.setBranch(user.getBranch());

        LoanRequest saved = loanRequestRepo.save(lr);

        emailService.send(
            user.getEmail(),
            "Loan Application Received",
            "Dear " + user.getCustomerProfile().getFullName() +
            ",\nYour loan request is submitted."
        );

        return saved;
    }



    // ================= USER LOANS =================

    @Override
    public List<LoanRequest> getLoansByUser(Long userId) {

        User current = getCurrentUser();

        if (!current.getId().equals(userId) &&
            current.getRole().getRoleName() != AppRole.ROLE_ADMIN) {

            throw new RuntimeException("Unauthorized");
        }

        return loanRequestRepo.findByUser_Id(userId);
    }


    // ================= PENDING LOANS =================

    @Override
    public List<LoanRequest> getPendingLoans() {

        User admin = getCurrentUser();

        // ADMIN → All branches
        if (admin.getRole().getRoleName()
                == AppRole.ROLE_ADMIN) {

            return loanRequestRepo
                    .findByStatus(Request.PENDING);
        }

        // MANAGER → Own branch
        if (admin.getRole().getRoleName()
                == AppRole.ROLE_MANAGER) {

            return loanRequestRepo
                    .findByStatusAndBranch_Id(
                        Request.PENDING,
                        admin.getBranch().getId()
                    );
        }

        throw new RuntimeException("Unauthorized");
    }


    // ================= APPROVE =================

    @Override
    public void approveLoan(Long id) {

        LoanRequest lr =
            loanRequestRepo.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Not found"));

        if (lr.getStatus() != Request.PENDING)
            throw new RuntimeException("Already processed");


        User admin = getCurrentUser();


        if (admin.getRole().getRoleName()
                != AppRole.ROLE_ADMIN)
            throw new UnauthorizedAccessException("Admin only");


        // Account
        Account account =
            accountRepo
            .findPrimaryAccountByUserIdForUpdate(
                lr.getUser().getId()
            )
            .orElseThrow(() ->
                new ResourceNotFoundException("No account"));


        // Update request
        lr.setStatus(Request.APPROVED);
        lr.setApprovedAt(LocalDateTime.now());
        lr.setApprovedBy(admin);

        loanRequestRepo.save(lr);


        // Create Loan
        BigDecimal principal = lr.getAmount();

        Loan loan = new Loan();

        loan.setPrincipal(principal);
        loan.setInterestRate(BigDecimal.valueOf(10.5));
        loan.setTenureMonths(lr.getTenureMonths());

        loan.setEmi(
            BigDecimal.valueOf(
                calculateEmi(
                    principal.doubleValue(),
                    10.5,
                    lr.getTenureMonths()
                )
            )
        );

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedAt(LocalDateTime.now());

        loan.setLoanReference("LN-" + UUID.randomUUID());

        loan.setAccount(account);

        loanRepo.save(loan);


        // Disbursement
        LoanDisbursement dis = new LoanDisbursement();

        dis.setLoanReference(loan.getLoanReference());
        dis.setAmount(principal);
        dis.setTenureMonths(lr.getTenureMonths());
        dis.setEmi(loan.getEmi());

        dis.setDisbursedAt(LocalDateTime.now());

        dis.setApprovedBy(admin.getEmail());

        dis.setCustomerName(
            lr.getUser()
              .getCustomerProfile()
              .getFullName()
        );

        dis.setBranchName(
            admin.getBranch().getBranchName()
        );

        dis.setLoan(loan);

        disbursementRepo.save(dis);


        // Credit
        ledgerService.postEntry(
            account,
            TransactionType.CREDIT,
            principal,
            "LOAN",
            "Loan Disbursed",
            "LOAN-" + UUID.randomUUID()
        );


        // Audit
        auditLogRepo.save(
            new AuditLog(
                null,
                "LOAN_APPROVED",
                admin.getId(),
                LocalDateTime.now()
            )
        );
    }


    // ================= REJECT =================

    @Override
    public void rejectLoan(Long id, String reason) {

        LoanRequest lr =
            loanRequestRepo.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Not found"));

        if (lr.getStatus() != Request.PENDING)
            throw new RuntimeException("Processed");


        lr.setStatus(Request.REJECTED);
        lr.setApprovedAt(LocalDateTime.now());
        lr.setApprovedBy(getCurrentUser());

        loanRequestRepo.save(lr);
    }


    // ================= EMI =================

    private Double calculateEmi(
        Double principal,
        Double rate,
        Integer months) {

        double r = rate / (12 * 100);

        return (principal * r *
               Math.pow(1 + r, months)) /
               (Math.pow(1 + r, months) - 1);
    }


    // ================= USER SUMMARY =================

    @Override
    public List<LoanSummaryDto> getUserLoanSummary(Long userId) {

        return disbursementRepo
                .findUserLoanSummary(userId);
    }


    // ================= ADMIN APPLICATIONS =================

    @Override
    public List<LoanSummaryDto> getAllLoanApplications() {

        return loanRequestRepo
                .fetchAllLoanSummaries();
    }
}
