// RoleInitializer.java  (FULL corrected)
package com.example.UnityTrustBank.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.UnityTrustBank.Entity.Branch;
import com.example.UnityTrustBank.Entity.Role;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.BranchRepo;
import com.example.UnityTrustBank.Repository.RoleRepo;
import com.example.UnityTrustBank.Repository.UserRepo;

@Component
public class RoleInitializer implements ApplicationRunner {

    @Autowired private UserRepo userRepo;
    @Autowired private RoleRepo roleRepo;
    @Autowired private BranchRepo branchRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // ✅ 1) Roles init (idempotent)
            initRole(AppRole.ROLE_USER);
            initRole(AppRole.ROLE_ADMIN);
            initRole(AppRole.ROLE_MANAGER);

            // ✅ 2) Default branch init (idempotent)
            Branch defaultBranch = initDefaultBranch();

            // ✅ 3) Manager init (idempotent: checks email OR mobile)
            initManager(defaultBranch);

            // ✅ 4) Admin password update (OPTIONAL) — safer: only update if user exists
            updateAdminPasswordIfPresent();

            System.out.println("ROLES AND MANAGER INITIALIZATION COMPLETED");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initRole(AppRole roleName) {
        try {
            roleRepo.findByRoleName(roleName).orElseGet(() -> {
                Role r = new Role();
                r.setRoleName(roleName);
                return roleRepo.save(r);
            });
        } catch (Exception e) {
            System.out.println("Could not initialize role " + roleName + ": " + e.getMessage());
        }
    }

    private Branch initDefaultBranch() {
        // If your BranchRepo has findById(1L) approach, keep it.
        // But DON'T setId(1L) when creating; let DB generate it.
        try {
            Optional<Branch> existing = branchRepo.findById(1L);
            if (existing.isPresent()) return existing.get();

            Branch b = new Branch();
            b.setBranchName("Main Branch");
            b.setBranchCode("MB001");
            b.setIfscCode("UTBI0000001");
            b.setAccountPrefix("MB001");
            b.setCity("Default City");
            b.setState("Default State");
            b.setActive(true);

            return branchRepo.save(b);
        } catch (Exception e) {
            // If findById(1L) is not reliable for your schema, you can switch to findByBranchCode("MB001")
            // but I’m keeping your approach.
            throw new RuntimeException("Could not initialize default branch: " + e.getMessage(), e);
        }
    }

    private void initManager(Branch defaultBranch) {
        try {
            String managerEmail = "manager@utb.com";
            String managerMobile = "9999999999";

            // ✅ IMPORTANT: mobile is unique, so check BOTH email and mobile
            boolean exists = userRepo.existsByEmail(managerEmail) || userRepo.existsByMobile(managerMobile);

            if (exists) {
                System.out.println("ℹ️ Manager already exists (email/mobile). Skipping create.");
                return;
            }

            Role managerRole = roleRepo.findByRoleName(AppRole.ROLE_MANAGER)
                    .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found"));

            User manager = new User();
            manager.setEmail(managerEmail);
            manager.setPassword(passwordEncoder.encode("Manager@123"));
            manager.setMobile(managerMobile);
            manager.setActive(true);
            manager.setRole(managerRole);
            manager.setBranch(defaultBranch);

            userRepo.save(manager);
            System.out.println("✅ Manager created successfully.");
        } catch (Exception e) {
            System.out.println("Could not initialize manager: " + e.getMessage());
        }
    }

    private void updateAdminPasswordIfPresent() {
        try {
            userRepo.findByEmail("admin@utb.com")
                    .ifPresent(user -> {
                        user.setPassword(passwordEncoder.encode("Admin@123"));
                        userRepo.save(user);
                    });
        } catch (Exception e) {
            System.out.println("Could not update admin password: " + e.getMessage());
        }
    }
}
