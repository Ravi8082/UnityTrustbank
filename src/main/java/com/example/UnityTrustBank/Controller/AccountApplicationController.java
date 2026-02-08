package com.example.UnityTrustBank.Controller;

import org.springframework.http.MediaType;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.AccountApplicationService;
import com.example.UnityTrustBank.dto.*;


@RestController
@RequestMapping("/account-applications")
public class AccountApplicationController {

    @Autowired
    private AccountApplicationService service;
    
    @Autowired
    private UserRepo userRepo;
    
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




    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<AccountApplicationResponseDto>> pending() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            return ResponseEntity.ok(service.pendingForBranch(user.getBranch().getId()));
        } else {
            return ResponseEntity.ok(service.pendingForManager());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approve(
            @PathVariable Long id) {

        service.approve(id);
        return ResponseEntity.ok("Application approved");
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> reject(
            @PathVariable Long id,
            @RequestParam String reason) {

        service.reject(id, reason);
        return ResponseEntity.ok("Application rejected");
    }
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/view-image")
    public ResponseEntity<Resource> viewImage(@RequestParam String path) {
        try {
            Path filePath = Paths.get(path);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) { 
            return ResponseEntity.internalServerError().build();
        }
}
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AccountApplicationResponseDto>> getAllApplications() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            // Admin sees only applications from their branch
            return ResponseEntity.ok(service.getAllApplicationsForBranch(user.getBranch().getId()));
        } else {
            // Manager sees all applications
            return ResponseEntity.ok(service.getAllApplicationsForManager());
        }
    }
}
