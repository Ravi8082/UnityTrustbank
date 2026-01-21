package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Service.UserService;
import com.example.UnityTrustBank.dto.*;

@RestController
@RequestMapping("/users")
@CrossOrigin("http://localhost:5175/")
public class UserController {

    @Autowired
    private UserService service;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/manager")
    public ResponseEntity<UserResponseDto> createManager(
            @RequestBody UserDto dto) {

        return ResponseEntity.ok(service.createManager(dto));
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getUser(id));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/customers")
    public ResponseEntity<List<UserResponseDto>> customers() {

        return ResponseEntity.ok(service.getAllCustomers());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id) {

        service.deactivateUser(id);
        return ResponseEntity.ok("User deactivated");
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/users/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody PasswordResetDto dto) {

        service.resetPassword(dto);
        return ResponseEntity.ok("Password reset successful");
    }

}
