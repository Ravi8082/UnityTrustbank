package com.example.UnityTrustBank.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Repository.UserRepo;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        System.out.println("LOAD USER: " + email);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        // ✅ IMPORTANT FIX HERE
        String role = user.getRole().getRoleName().name();

        System.out.println("ROLE = " + role);
        System.out.println("ACTIVE = " + user.isActive());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(role)
                .disabled(!user.isActive())
                .build();
    }
}
