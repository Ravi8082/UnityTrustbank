package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Service.TransactionService;
import com.example.UnityTrustBank.dto.TransactionResponseDto;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService service;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/statement/{accountId}")
    public ResponseEntity<List<TransactionResponseDto>> statement(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(service.getStatement(accountId));
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/mini/{accountId}")
    public ResponseEntity<List<TransactionResponseDto>> mini(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(service.getMiniStatement(accountId));
    }
}
