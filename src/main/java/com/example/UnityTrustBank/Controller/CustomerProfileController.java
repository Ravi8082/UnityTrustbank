package com.example.UnityTrustBank.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Entity.CustomerProfile;
import com.example.UnityTrustBank.Service.CustomerProfileService;
import com.example.UnityTrustBank.dto.CustomerProfileDto;

@RestController
@RequestMapping("/api/customer-profiles")
public class CustomerProfileController {

    @Autowired
    private CustomerProfileService profileService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<CustomerProfile> createProfile(
            @PathVariable Long userId,
            @RequestBody CustomerProfileDto customerProfileDto) {

        CustomerProfile profile =
                profileService.createProfile(userId, customerProfileDto);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/user/{userId}/documents")
    public ResponseEntity<CustomerProfile> updateDocuments(
            @PathVariable Long userId,
            @RequestParam String aadhaar,
            @RequestParam String pan) {

        CustomerProfile profile =
                profileService.updateDocuments(userId, aadhaar, pan);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/user/{userId}/verify-aadhaar")
    public ResponseEntity<String> verifyAadhaar(
            @PathVariable Long userId,
            @RequestParam Long managerId) {

        profileService.verifyAadhaar(userId, managerId);
        return ResponseEntity.ok("Aadhaar verified successfully");
    }

    @PutMapping("/user/{userId}/verify-pan")
    public ResponseEntity<String> verifyPan(
            @PathVariable Long userId,
            @RequestParam Long managerId) {

        profileService.verifyPan(userId, managerId);
        return ResponseEntity.ok("PAN verified successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomerProfile> getProfile(
            @PathVariable Long userId) {

        CustomerProfile profile =
                profileService.getProfileByUser(userId);
        return ResponseEntity.ok(profile);
    }
}
