package com.example.UnityTrustBank.security;

import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Entity.Role;
import com.example.UnityTrustBank.Entity.Branch;

import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Repository.RoleRepo;
import com.example.UnityTrustBank.Repository.BranchRepo;

import com.example.UnityTrustBank.Enum.AppRole;

@Configuration
public class DataInitializer {

    @Autowired private UserRepo userRepo;
    @Autowired private RoleRepo roleRepo;
    @Autowired private BranchRepo branchRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        try {
            // 1) Ensure roles exist (idempotent)
            Role userRole = ensureRole(AppRole.ROLE_USER);
            Role adminRole = ensureRole(AppRole.ROLE_ADMIN);
            Role managerRole = ensureRole(AppRole.ROLE_MANAGER);

            // 2) Ensure default branch exists (idempotent)
            Branch branch = ensureBranch();

            // 3) Ensure admin exists (idempotent, avoids crash)
            ensureAdmin(adminRole, branch);

            System.out.println("✅ DATA INITIALIZATION COMPLETED");
        } catch (Exception e) {
            // ✅ Never crash the app on startup in cloud
            System.out.println("⚠️ DataInitializer skipped due to error: " + e.getMessage());
        }
    }

    private Role ensureRole(AppRole roleName) {
        return roleRepo.findByRoleName(roleName).orElseGet(() -> {
            Role r = new Role();
            r.setRoleName(roleName);
            Role saved = roleRepo.save(r);
            System.out.println("✅ ROLE CREATED: " + roleName);
            return saved;
        });
    }

    private Branch ensureBranch() {
        return branchRepo.findByBranchCode("KHR001").orElseGet(() -> {
            Branch b = new Branch();
            b.setBranchName("Kharihani");
            b.setBranchCode("KHR001");
            b.setIfscCode("UTB0001KHR");
            b.setAccountPrefix("KHR");
            b.setCity("Kharihani");
            b.setState("Uttar Pradesh");
            b.setActive(true);
            Branch saved = branchRepo.save(b);
            System.out.println("✅ DEFAULT BRANCH CREATED");
            return saved;
        });
    }

    private void ensureAdmin(Role adminRole, Branch branch) {
        String email = "admin@utb.com";

        Optional<User> adminOpt = userRepo.findByEmail(email);

        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode("Admin@123"));

            // IMPORTANT: unique mobile constraint? then set a unique mobile too.
            // admin.setMobile("9000000001");

            admin.setRole(adminRole);
            admin.setBranch(branch);
            admin.setActive(true);
            admin.setPasswordResetRequired(false);

            userRepo.save(admin);
            System.out.println("✅ DEFAULT ADMIN CREATED");
            return;
        }

        // Update existing admin (optional)
        User admin = adminOpt.get();
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(adminRole);
        admin.setBranch(branch);
        userRepo.save(admin);

        System.out.println("✅ ADMIN UPDATED");
    }
}
