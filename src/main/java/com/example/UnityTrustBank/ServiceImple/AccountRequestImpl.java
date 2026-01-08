package com.example.UnityTrustBank.ServiceImple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.UnityTrustBank.Entity.AccountRequest;
import com.example.UnityTrustBank.Entity.Branch;
import com.example.UnityTrustBank.Entity.CustomerProfile;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.AccountRequestRepo;
import com.example.UnityTrustBank.Repository.BranchRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.AccountRequestService;

@Service
public class AccountRequestImpl implements AccountRequestService {

    @Autowired
    private AccountRequestRepo accountRequestRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Override
    public void createAccountRequest(Long userId, Long branchId, String accountType) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = user.getCustomerProfile();
        if (profile == null || profile.getDob() == null) {
            throw new RuntimeException("Customer profile or DOB missing");
        }
        int age = Period.between(profile.getDob(), LocalDate.now()).getYears();
        if (age < 18) {
            profile.setBranchVisitRequired(true);
            throw new RuntimeException(
                    "Customer below 18. Branch visit required for account opening");
        }
        if (profile.getAadhaar() == null || profile.getPan() == null) {
            throw new RuntimeException("Aadhaar and PAN are mandatory");
        }
        if (accountRepo.existsByUser_Id(userId)) {
            throw new RuntimeException("Account already exists for this user");
        }
        if (accountRequestRepo.existsByUser_IdAndStatus(userId, Request.PENDING)) {
            throw new RuntimeException("Account request already pending");
        }

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        AccountRequest request = new AccountRequest();
        request.setUser(user);
        request.setBranch(branch);
        request.setAccountType(accountType);
        request.setStatus(Request.PENDING);
        request.setRequestDate(LocalDateTime.now());

        accountRequestRepo.save(request);
    }
    @Override
    public void approveAccountRequest(Long requestId, Long managerId) {

        AccountRequest request = getPendingRequest(requestId);
        User manager = getManager(managerId);

        CustomerProfile profile = request.getUser().getCustomerProfile();
        if (!profile.isAadhaarVerified() || !profile.isPanVerified()) {
            throw new RuntimeException("Documents not verified by manager");
        }

        request.setStatus(Request.APPROVED);
        request.setApprovedBy(manager);
        request.setApprovedDate(LocalDateTime.now());
        request.setRejectionReason(null);

        accountRequestRepo.save(request);
    }
    @Override
    public void rejectAccountRequest(Long requestId, Long managerId, String reason) {

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Rejection reason is mandatory");
        }

        AccountRequest request = getPendingRequest(requestId);
        User manager = getManager(managerId);

        request.setStatus(Request.REJECTED);
        request.setApprovedBy(manager);
        request.setApprovedDate(LocalDateTime.now());
        request.setRejectionReason(reason);

        accountRequestRepo.save(request);
    }
    @Override
    public List<AccountRequest> getPendingRequestsByBranch(Long branchId) {
        return accountRequestRepo
                .findByBranch_IdAndStatus(branchId, Request.PENDING);
    }
    @Override
    public List<AccountRequest> getRequestsByUser(Long userId) {
        return accountRequestRepo.findByUser_Id(userId);
    }
    private AccountRequest getPendingRequest(Long requestId) {

        AccountRequest request = accountRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Account request not found"));

        if (request.getStatus() != Request.PENDING) {
            throw new RuntimeException("Account request already processed");
        }
        return request;
    }

    private User getManager(Long managerId) {

        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() == null ||
            manager.getRole().getRoleName() != AppRole.ROLE_ADMIN) {
            throw new RuntimeException("Only manager can approve or reject requests");
        }
        return manager;
    }
}
