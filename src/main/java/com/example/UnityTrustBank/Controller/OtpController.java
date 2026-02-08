package com.example.UnityTrustBank.Controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.ServiceImple.EmailService;
import com.example.UnityTrustBank.dto.OtpStore;
@RestController
@RequestMapping("/auth")
public class OtpController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepo userRepo;   

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email) {

        if (userRepo.existsByEmail(email)) {
            throw new RuntimeException("Email already registered. Use login instead.");
        }

        int otp = new Random().nextInt(900000) + 100000;
        OtpStore.save(email, otp);

        emailService.send(
            email,
            "Email Verification OTP",
            "Your OTP is: " + otp + "\nValid for 2 minutes."
        );

        return "OTP sent to registered email";
    }

    @PostMapping("/verify-otp")
    public String verify(@RequestParam String email, @RequestParam int otp) {

        if (!OtpStore.verify(email, otp))
            throw new RuntimeException("Invalid or expired OTP");

        return "OTP verified";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email) {

        if (!userRepo.existsByEmail(email)) {
            throw new RuntimeException("No account found with this email");
        }

        OtpStore.clear(email);

        int otp = new Random().nextInt(900000) + 100000;
        OtpStore.save(email, otp);

        emailService.send(
            email,
            "UnityTrust Bank - OTP Verification",
            "Your new OTP is: " + otp +
            "\n\nValid for 2 minutes." +
            "\nDo not share this OTP with anyone."
        );

        return "OTP resent successfully";
    }
}
