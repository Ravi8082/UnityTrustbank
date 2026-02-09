
package com.example.UnityTrustBank.security;

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

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        // ============================
        // 1️⃣ CREATE BRANCH IF NOT EXISTS
        // ============================

        Branch branch = branchRepo
            .findByBranchCode("KHR001")
            .orElseGet(() -> {

                Branch b = new Branch();

                b.setBranchName("Kharihani");
                b.setBranchCode("KHR001");
                b.setIfscCode("UTB0001KHR");
                b.setAccountPrefix("KHR");

                b.setCity("Kharihani");
                b.setState("Uttar Pradesh");

                b.setActive(true);

                branchRepo.save(b);

                System.out.println("DEFAULT BRANCH CREATED");

                return b;
            });


        // ============================
        // 2️⃣ GET ROLE_ADMIN
        // ============================

        Role adminRole = roleRepo
            .findByRoleName(AppRole.ROLE_ADMIN)
            .orElseThrow(() ->
                new RuntimeException("ROLE_ADMIN not found in DB"));


        // ============================
        // 3️⃣ CREATE / UPDATE ADMIN
        // ============================

        Optional<User> adminOpt =
            userRepo.findByEmail("admin@utb.com");


        // ---- Create admin if missing ----
        if (adminOpt.isEmpty()) {

            User admin = new User();

            admin.setEmail("admin@utb.com");
            admin.setPassword(
                passwordEncoder.encode("Admin@123")
            );

            admin.setRole(adminRole);
            admin.setBranch(branch);

            admin.setActive(true);
            admin.setPasswordResetRequired(false);

            userRepo.save(admin);

            System.out.println("DEFAULT ADMIN CREATED");

            return;
        }


        // ---- Update admin if exists ----
        User admin = adminOpt.get();

        admin.setPassword(
            passwordEncoder.encode("Admin@123")
        );

        admin.setRole(adminRole);
        admin.setBranch(branch);

        userRepo.save(admin);

        System.out.println("ADMIN UPDATED");
    }
}
