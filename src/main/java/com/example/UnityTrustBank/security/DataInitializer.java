package com.example.UnityTrustBank.security;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Repository.UserRepo;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void fixAdminPasswordOnce() {

        User user = userRepo.findByEmail("admin@utb.com")
                .orElseThrow(() ->
                    new RuntimeException("Admin not found"));

        user.setPassword(
                passwordEncoder.encode("Admin@123")
        );

        userRepo.save(user);

        System.out.println("ADMIN PASSWORD RESET DONE");
    }
}
