package com.example.UnityTrustBank.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.ServiceImple.EmailService;
import com.example.UnityTrustBank.dto.LoginRequestDto;
import com.example.UnityTrustBank.dto.OtpStore;
import com.example.UnityTrustBank.security.JwtUtil;
@RestController
@RequestMapping("/auth")

public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Return structured response with user details
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("mobile", user.getMobile());
        response.put("role", user.getRole().getRoleName().toString());
        response.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
        response.put("branchName", user.getBranch() != null ? user.getBranch().getBranchName() : null);
        
        // Add customer profile details if available
        if (user.getCustomerProfile() != null) {
            response.put("fullName", user.getCustomerProfile().getFullName());
            response.put("gender", user.getCustomerProfile().getGender());
            response.put("branchName", user.getBranch() != null ? user.getBranch().getBranchName() : null);
        } else {
            response.put("fullName", user.getEmail().split("@")[0]); // Fallback to email username
            response.put("gender", null);
            response.put("branchName", user.getBranch() != null ? user.getBranch().getBranchName() : null);
        }
        
        return ResponseEntity.ok(response); 
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {

        try {

            Authentication auth =
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                    )
                );

            // Fetch user from DB
            User user =
                userRepo.findByEmail(dto.getEmail())
                .orElseThrow();

            String role =
                auth.getAuthorities()
                    .stream()
                    .findFirst()
                    .orElseThrow()
                    .getAuthority();

            String token =
                jwtUtil.generateToken(
                    user.getId(),
                    user.getEmail(),
                    role
                );

            return ResponseEntity.ok(token);

        } catch (Exception e) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid credentials");
        }
    }
    
    // Forgot Password - Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));
        
        int otp = new Random().nextInt(900000) + 100000;
        OtpStore.save(email, otp);
        
        emailService.send(
            email,
            "Password Reset OTP - UnityTrust Bank",
            "Your OTP for password reset is: " + otp + "\nValid for 5 minutes.\nIf you did not request this, please ignore this email."
        );
        
        return ResponseEntity.ok("OTP sent to your registered email");
    }
    
    // Verify OTP and Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam int otp,
            @RequestParam String newPassword) {
        
        if (!OtpStore.verify(email, otp)) {
            return ResponseEntity.status(400).body("Invalid or expired OTP");
        }
        
        if (newPassword.length() < 8) {
            return ResponseEntity.status(400).body("Password must be at least 8 characters");
        }
        
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        
        emailService.send(
            email,
            "Password Changed Successfully",
            "Your UnityTrust Bank account password has been changed successfully.\nIf you did not make this change, please contact support immediately."
        );
        
        return ResponseEntity.ok("Password reset successful");
    }
}
