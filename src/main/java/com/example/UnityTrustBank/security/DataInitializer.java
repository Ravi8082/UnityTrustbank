
package com.example.UnityTrustBank.security;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Enum.AppRole;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdmin() {

        Optional<User> adminOpt =
                userRepo.findByEmail("admin@utb.com");

        // ✅ Agar admin nahi mila → create karo
        if (adminOpt.isEmpty()) {

            User admin = new User();

            admin.setEmail("admin@utb.com");
            admin.setPassword(
                    passwordEncoder.encode("Admin@123")
            );

            // ✅ ENUM ROLE
            admin.setRole(AppRole.ROLE_ADMIN);

            userRepo.save(admin);

            System.out.println("DEFAULT ROLE_ADMIN CREATED");

            return;
        }

        // ✅ Agar admin hai → password reset
        User admin = adminOpt.get();

        admin.setPassword(
                passwordEncoder.encode("Admin@123")
        );

        admin.setRole(AppRole.ROLE_ADMIN); // ensure role

        userRepo.save(admin);

        System.out.println("ROLE_ADMIN UPDATED");
    }
}
