
package com.example.UnityTrustBank.security;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Entity.Role;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Repository.RoleRepo;
import com.example.UnityTrustBank.Enum.AppRole;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdmin() {

        // 1️⃣ Get ROLE_ADMIN from DB
        Role adminRole = roleRepo
                .findByRoleName(AppRole.ROLE_ADMIN)
                .orElseThrow(() ->
                        new RuntimeException("ROLE_ADMIN not found in roles table"));

        // 2️⃣ Check admin user
        Optional<User> adminOpt =
                userRepo.findByEmail("admin@utb.com");

        // 3️⃣ Create if not exists
        if (adminOpt.isEmpty()) {

            User admin = new User();

            admin.setEmail("admin@utb.com");
            admin.setPassword(
                    passwordEncoder.encode("Admin@123")
            );

            admin.setRole(adminRole);
            admin.setActive(true);
            admin.setPasswordResetRequired(false);

            userRepo.save(admin);

            System.out.println("DEFAULT ADMIN CREATED");

            return;
        }

        // 4️⃣ Update if exists
        User admin = adminOpt.get();

        admin.setPassword(
                passwordEncoder.encode("Admin@123")
        );

        admin.setRole(adminRole);

        userRepo.save(admin);

        System.out.println("ADMIN UPDATED");
    }
}
