package com.example.UnityTrustBank.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
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
@Profile("init") // ✅ runs ONLY when SPRING_PROFILES_ACTIVE=init
public class RoleInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleInitializer.class);

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

            // ✅ 2) Default branch init (idempotent) - use branchCode, not ID
            Branch defaultBranch = initDefaultBranchByCode("MB001");

            // ✅ 3) Manager init (idempotent: checks email OR mobile)
            initManager(defaultBranch);

            // ✅ 4) Admin password update (OPTIONAL) — safer: do NOT reset every start
            // updateAdminPasswordIfPresent(); // 🔒 Recommended to keep OFF in production

            log.info("✅ ROLES AND MANAGER INITIALIZATION COMPLETED");
        } catch (Exception e) {
            // ✅ Don't swallow: log full stack so you can debug.
            log.error("❌ Error during initialization", e);
            // ✅ Do NOT exit/crash the whole service in production:
            // If you want fail-fast during init profile, you can uncomment:
            // throw e;
        }
    }

    private void initRole(AppRole roleName) {
        try {
            roleRepo.findByRoleName(roleName).orElseGet(() -> {
                Role r = new Role();
                r.setRoleName(roleName);
                Role saved = roleRepo.save(r);
                log.info("✅ Created role: {}", roleName);
                return saved;
            });
        } catch (Exception e) {
            log.error("❌ Could not initialize role {}: {}", roleName, e.getMessage(), e);
        }
    }

    private Branch initDefaultBranchByCode(String branchCode) {
        try {
            // ✅ Preferred: find by branchCode (stable + idempotent)
            Optional<Branch> existing = branchRepo.findByBranchCode(branchCode);
            if (existing.isPresent()) {
                log.info("ℹ️ Default branch already exists: {}", branchCode);
                return existing.get();
            }

            Branch b = new Branch();
            b.setBranchName("Main Branch");
            b.setBranchCode(branchCode);
            b.setIfscCode("UTBI0000001");
            b.setAccountPrefix(branchCode);
            b.setCity("Default City");
            b.setState("Default State");
            b.setActive(true);

            Branch saved = branchRepo.save(b);
            log.info("✅ Created default branch: {}", branchCode);
            return saved;

        } catch (Exception e) {
            log.error("❌ Could not initialize default branch {}: {}", branchCode, e.getMessage(), e);
            // Return a safe fallback by throwing (init profile only) or handle as needed
            throw new RuntimeException("Default branch init failed: " + e.getMessage(), e);
        }
    }

    private void initManager(Branch defaultBranch) {
        try {
            String managerEmail = "manager@utb.com";
            String managerMobile = "9999999999";

            // ✅ IMPORTANT: mobile is unique, so check BOTH email and mobile
            boolean exists = userRepo.existsByEmail(managerEmail) || userRepo.existsByMobile(managerMobile);

            if (exists) {
                log.info("ℹ️ Manager already exists (email/mobile). Skipping create.");
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
            log.info("✅ Manager created successfully.");

        } catch (Exception e) {
            log.error("❌ Could not initialize manager: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    private void updateAdminPasswordIfPresent() {
        try {
            userRepo.findByEmail("admin@utb.com")
                    .ifPresent(user -> {
                        user.setPassword(passwordEncoder.encode("Admin@123"));
                        userRepo.save(user);
                        log.info("✅ Updated admin password.");
                    });
        } catch (Exception e) {
            log.error("❌ Could not update admin password: {}", e.getMessage(), e);
        }
    }
}
