package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.UserService;
import com.example.UnityTrustBank.dto.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;
    
    @Autowired
    private UserRepo userRepo;

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @PostMapping("/create-admin")
    public ResponseEntity<UserResponseDto> createAdmin(
            @RequestBody UserDto dto) {

        return ResponseEntity.ok(service.createManager(dto));
    }
    
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @PostMapping("/create-admin-with-profile")
    public ResponseEntity<UserResponseDto> createAdminWithProfile(
            @RequestBody AdminCreateDto dto) {

        return ResponseEntity.ok(service.createAdminWithProfile(dto));
    }
    
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        // Managers can see all users, admins can only see users in their branch
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            return ResponseEntity.ok(service.getAllUsersInBranch(user.getBranch().getId()));
        } else {
            return ResponseEntity.ok(service.getAllUsers());
        }
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getUser(id));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/customers")
    public ResponseEntity<List<UserResponseDto>> customers() {
        // Managers can see all customers, admins can only see customers in their branch
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            return ResponseEntity.ok(service.getAllCustomersInBranch(user.getBranch().getId()));
        } else {
            return ResponseEntity.ok(service.getAllCustomers());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id) {
        // Admins can only deactivate users in their branch, managers can deactivate any user
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (currentUser.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            service.deactivateUserInBranch(id, currentUser.getBranch().getId());
        } else {
            service.deactivateUser(id);
        }
        return ResponseEntity.ok("User deactivated");
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/users/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody PasswordResetDto dto) {

        service.resetPassword(dto);
        return ResponseEntity.ok("Password reset successful");
    }
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping("/admin/full")
    public ResponseEntity<List<AdminUserDto>> getAllUsersFull() {

        return ResponseEntity.ok(
            service.getAllUsersWithDetails()
        );
    }


}