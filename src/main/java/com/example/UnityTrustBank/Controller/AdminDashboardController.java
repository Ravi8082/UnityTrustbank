package com.example.UnityTrustBank.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Enum.ApplicationStatus;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.Repository.AccountApplicationRepo;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.AtmRequestRepo;
import com.example.UnityTrustBank.Repository.BranchRepo;
import com.example.UnityTrustBank.Repository.CustomerProfileRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.AccountApplicationService;
import com.example.UnityTrustBank.dto.AccountApplicationResponseDto;

@RestController
@RequestMapping("/admin-dashboard")
public class AdminDashboardController {

    @Autowired private UserRepo userRepo;
    @Autowired private AccountRepo accountRepo;
    @Autowired private AccountApplicationRepo appRepo;
    @Autowired private AtmRequestRepo atmRepo;
    @Autowired private AccountApplicationService appService;
    @Autowired private CustomerProfileRepo customerProfileRepo;
    @Autowired private BranchRepo branchRepo;

    private User getCurrentAdmin() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getRoleName() != AppRole.ROLE_ADMIN && user.getRole().getRoleName() != AppRole.ROLE_MANAGER)
            throw new RuntimeException("Admin access required");

        return user;
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {

        User currentUser = getCurrentAdmin();
        
        Map<String, Long> stats = new HashMap<>();
        
        if (currentUser.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            // Admin can only access their own branch data
            Long branchId = currentUser.getBranch().getId();
            
            stats.put("totalUsers",
                    userRepo.findByBranch_Id(branchId).stream().count());

            stats.put("totalAccounts",
                    accountRepo.findByBranch_Id(branchId).stream().count());

            stats.put("pendingApplications",
                    appRepo.findByBranch_IdAndStatus(
                            branchId, ApplicationStatus.SUBMITTED).stream().count());

            stats.put("pendingAtmRequests",
                    atmRepo.findByAccount_Branch_IdAndStatus(
                            branchId, Request.PENDING).stream().count());
        } else if (currentUser.getRole().getRoleName() == AppRole.ROLE_MANAGER) {
            // Manager can access statistics across all branches
            stats.put("totalUsers",
                    userRepo.countByBranch_IdIsNotNull());

            stats.put("totalAccounts",
                    accountRepo.countByBranch_IdIsNotNull());

            stats.put("pendingApplications",
                    appRepo.countByStatus(ApplicationStatus.SUBMITTED));

            stats.put("pendingAtmRequests",
                    atmRepo.countByStatus(Request.PENDING));
        } else {
            throw new RuntimeException("Unauthorized access");
        }

        return ResponseEntity.ok(stats);
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/applications/{id}")
    public ResponseEntity<AccountApplicationResponseDto> getApplicationDetails(@PathVariable Long id) {
        User currentUser = getCurrentAdmin();
        
        var application = appRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        // Admin can only access applications from their own branch
        // Manager can access all applications
        if (currentUser.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            if (!currentUser.getBranch().getId().equals(application.getBranch().getId())) {
                throw new RuntimeException("Unauthorized access to this application");
            }
        }
        
        return ResponseEntity.ok(appService.getApplicationById(id));
    }
    
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getAdminProfile() {
        User currentUser = getCurrentAdmin();
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", currentUser.getId());
        profile.put("email", currentUser.getEmail());
        profile.put("mobile", currentUser.getMobile());
        profile.put("role", currentUser.getRole().getRoleName().name());
        profile.put("branchId", currentUser.getBranch().getId());
        profile.put("branchName", currentUser.getBranch().getBranchName());
        
        // Get customer profile for gender and full name
        var customerProfile = customerProfileRepo.findByUser_Id(currentUser.getId());
        if (customerProfile.isPresent()) {
            profile.put("fullName", customerProfile.get().getFullName());
            profile.put("gender", customerProfile.get().getGender());
            profile.put("fatherName", customerProfile.get().getFatherName());
            profile.put("address", customerProfile.get().getAddress());
            profile.put("aadhaar", customerProfile.get().getAadhaar());
            profile.put("pan", customerProfile.get().getPan());
            profile.put("profileImagePath", customerProfile.get().getProfileImagePath());
        } else {
            profile.put("fullName", currentUser.getEmail().split("@")[0]);
            profile.put("gender", null);
            profile.put("fatherName", null);
            profile.put("address", null);
            profile.put("aadhaar", null);
            profile.put("pan", null);
            profile.put("profileImagePath", null);
        }
        
        return ResponseEntity.ok(profile);
    }
}