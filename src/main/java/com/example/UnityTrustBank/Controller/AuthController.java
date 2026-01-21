package com.example.UnityTrustBank.Controller;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.dto.LoginRequestDto;
import com.example.UnityTrustBank.security.JwtUtil;
@RestController
@RequestMapping("/auth")
@CrossOrigin("http://localhost:5175/")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDto dto) {
    	System.out.println("Email "+dto.getEmail());
    	System.out.println(
    		    new BCryptPasswordEncoder().encode("Admin@123")
    		);


        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    dto.getEmail(),
                    dto.getPassword()
                )
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(401)
                    .body("Invalid credentials");
        }

        return ResponseEntity.ok(
                jwtUtil.generateToken(dto.getEmail())
        );
    }
}
