package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Entity.AtmRequest;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.Repository.AtmRequestRepo;
import com.example.UnityTrustBank.Service.AtmService;
import com.example.UnityTrustBank.dto.AtmPinResetDto;
import com.example.UnityTrustBank.dto.AtmRejectDto;
import com.example.UnityTrustBank.dto.AtmWithdrawDto;

@CrossOrigin("http://localhost:5175/")
@RestController
@RequestMapping("/atm")
public class AtmController {

    @Autowired
    private AtmService atmService;

    @Autowired
    private AtmRequestRepo atmRequestRepo;

    // ========== USER ==========

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/request/{accountNumber}")
    public ResponseEntity<String> requestAtm(
            @PathVariable String accountNumber) {

        atmService.requestAtm(accountNumber);
        return ResponseEntity.ok("ATM request submitted");
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @RequestBody AtmWithdrawDto dto) {

        atmService.withdraw(
                dto.getCardId(),
                dto.getPin(),
                dto.getAmount()
        );
        return ResponseEntity.ok("Withdrawal successful");
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/reset-pin")
    public ResponseEntity<String> resetPin(
            @RequestBody AtmPinResetDto dto) {

        atmService.resetPin(
                dto.getCardId(),
                dto.getOldPin(),
                dto.getNewPin()
        );
        return ResponseEntity.ok("PIN reset successful");
    }

    // ========== ADMIN ==========

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/requests/pending")
    public ResponseEntity<List<AtmRequest>> pendingRequests() {
        return ResponseEntity.ok(
                atmRequestRepo.findByStatus(Request.PENDING)
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<String> approveAtm(@PathVariable Long requestId) {
        atmService.approveAtm(requestId);
        return ResponseEntity.ok("ATM request approved");
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<String> rejectAtm(
            @PathVariable Long requestId,
            @RequestBody AtmRejectDto dto) {

        atmService.rejectAtm(requestId, dto.getReason());
        return ResponseEntity.ok("ATM request rejected");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/requests/pending/count")
    public ResponseEntity<List<AtmRequest>> pendingCount() {
        return ResponseEntity.ok(
                atmRequestRepo.countByStatus(Request.PENDING)
        );
    }

}
