package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.UnityTrustBank.Service.AccountService;
import com.example.UnityTrustBank.dto.AccountResponseDto;
@CrossOrigin("http://localhost:5175/")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService service;
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> get(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getAccount(id));
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDto>> userAccounts(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                service.getAccountsForUser(userId));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/freeze")
    public ResponseEntity<String> freeze(
            @PathVariable Long id) {

        service.freezeAccount(id);
        return ResponseEntity.ok("Account frozen");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/unfreeze")
    public ResponseEntity<String> unfreeze(
            @PathVariable Long id) {

        service.unfreezeAccount(id);
        return ResponseEntity.ok("Account activated");
    }
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/{id}/close")
    public ResponseEntity<String> close(@PathVariable Long id) {
        service.closeAccount(id);
        return ResponseEntity.ok("Account closed");
    }

}
