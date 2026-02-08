package com.example.UnityTrustBank.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Service.CustomerProfileService;
import com.example.UnityTrustBank.dto.CustomerProfileCreateDto;
import com.example.UnityTrustBank.dto.CustomerProfileResponseDto;

@RestController
@RequestMapping("/customer-profile")
public class CustomerProfileController {

    @Autowired
    private CustomerProfileService service;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/user/{userId}")
    public ResponseEntity<CustomerProfileResponseDto> create(
            @PathVariable Long userId,
            @RequestBody CustomerProfileCreateDto dto) {

        return ResponseEntity.ok(service.createProfile(userId, dto));
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomerProfileResponseDto> get(
            @PathVariable Long userId) {

        return ResponseEntity.ok(service.getProfile(userId));
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/user/{userId}/documents")
    public ResponseEntity<String> updateDocs(
            @PathVariable Long userId,
            @RequestParam String aadhaar,
            @RequestParam String pan) {

        service.updateDocuments(userId, aadhaar, pan);
        return ResponseEntity.ok("Documents updated");
    }

    // MANAGER verifies
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/user/{userId}/verify-aadhaar")
    public ResponseEntity<String> verifyAadhaar(
            @PathVariable Long userId) {

        service.verifyAadhaar(userId);
        return ResponseEntity.ok("Aadhaar verified");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/user/{userId}/verify-pan")
    public ResponseEntity<String> verifyPan(
            @PathVariable Long userId) {

        service.verifyPan(userId);
        return ResponseEntity.ok("PAN verified");
    }
}
