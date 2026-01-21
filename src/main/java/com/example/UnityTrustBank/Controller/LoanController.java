package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.UnityTrustBank.Entity.LoanRequest;
import com.example.UnityTrustBank.Service.LoanService;
@RestController
@RequestMapping("/loans")
@CrossOrigin("http://localhost:5175/")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/apply")
    public ResponseEntity<LoanRequest> applyLoan(@RequestBody LoanRequest dto) {
        // Accept LoanRequest entity directly
        return ResponseEntity.ok(loanService.applyLoan(dto));
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanRequest>> getUserLoans(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUser(userId));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<LoanRequest>> pendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approve(@PathVariable Long id) {
        loanService.approveLoan(id);
        return ResponseEntity.ok("Loan approved");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> reject(@PathVariable Long id,
                                         @RequestParam String reason) {
        loanService.rejectLoan(id, reason);
        return ResponseEntity.ok("Loan rejected");
    }
}
