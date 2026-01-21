package com.example.UnityTrustBank.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Enum.ApplicationStatus;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.Repository.AccountApplicationRepo;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.AtmRequestRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
@RestController
@RequestMapping("/admin-dashboard")
@CrossOrigin("http://localhost:5175/")
public class AdminDashboardController {

    @Autowired private UserRepo userRepo;
    @Autowired private AccountRepo accountRepo;
    @Autowired private AccountApplicationRepo appRepo;
    @Autowired private AtmRequestRepo atmRepo;

    private User getCurrentAdmin() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getRoleName() != AppRole.ROLE_ADMIN)
            throw new RuntimeException("Admin access required");

        return user;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {

        User admin = getCurrentAdmin();
        Long branchId = admin.getBranch().getId();

        Map<String, Long> stats = new HashMap<>();

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

        return ResponseEntity.ok(stats);
    }
}
