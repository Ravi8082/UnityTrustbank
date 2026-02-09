
package com.example.UnityTrustBank.security;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Repository.UserRepo;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void fixAdminPasswordOnce() {

        Optional<User> adminOpt =
                userRepo.findByEmail("admin@utb.com");

        // ✅ If admin not found → create one
        if (adminOpt.isEmpty()) {

            User admin = new User();

            admin.setEmail("admin@utb.com");
            admin.setPassword(
                    passwordEncoder.encode("Admin@123")
            );
            admin.setRole("ADMIN");

            userRepo.save(admin);

            System.out.println("DEFAULT ADMIN CREATED");

            return;
        }

        // ✅ If admin exists → reset password
        User admin = adminOpt.get();

        admin.setPassword(
                passwordEncoder.encode("Admin@123")
        );

        userRepo.save(admin);

        System.out.println("ADMIN PASSWORD RESET DONE");
    }
}
