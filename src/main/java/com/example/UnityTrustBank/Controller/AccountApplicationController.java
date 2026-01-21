package com.example.UnityTrustBank.Controller;

import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.UnityTrustBank.Service.AccountApplicationService;
import com.example.UnityTrustBank.dto.*;
@CrossOrigin("http://localhost:5175/")

@RestController
@RequestMapping("/account-applications")
public class AccountApplicationController {

    @Autowired
    private AccountApplicationService service;
    
    @PostMapping("/apply")
    public ResponseEntity<AccountApplicationResponseDto> apply(
            @RequestBody AccountApplicationCreateDto dto) {

        return ResponseEntity.ok(service.apply(dto));
    }
    @PutMapping(
    	    value = "/{id}/upload-kyc",
    	    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    	)
    	public ResponseEntity<?> uploadKycImages(
    	        @PathVariable Long id,
    	        @RequestParam("profileImage") MultipartFile profileImage,
    	        @RequestParam("aadhaarImage") MultipartFile aadhaarImage,
    	        @RequestParam("panImage") MultipartFile panImage
    	) {
    	    service.uploadKycImages(id, profileImage, aadhaarImage, panImage);

    	    return ResponseEntity.ok(
    	        Map.of("message", "KYC images uploaded successfully")
    	    );
    	}





    @PostMapping("/multipart-test")
    public String test(@RequestParam MultipartFile file) {
        return file.getOriginalFilename();
    }




    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<AccountApplicationResponseDto>> pending() {

        return ResponseEntity.ok(service.pendingForManager());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approve(
            @PathVariable Long id) {

        service.approve(id);
        return ResponseEntity.ok("Application approved");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> reject(
            @PathVariable Long id,
            @RequestParam String reason) {

        service.reject(id, reason);
        return ResponseEntity.ok("Application rejected");
    }
}
