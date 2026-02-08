package com.example.UnityTrustBank.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Entity.AtmReceipt;
import com.example.UnityTrustBank.Entity.AtmRequest;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.Repository.AtmRequestRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.AtmService;
import com.example.UnityTrustBank.dto.*;

@RestController
@RequestMapping("/atm")
public class AtmController {

    @Autowired
    private AtmService atmService;

    @Autowired
    private AtmRequestRepo atmRequestRepo;

    @Autowired
    private UserRepo userRepo;

    // ================= USER =================

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/request/{accountNumber}")
    public ResponseEntity<?> requestAtm(@PathVariable String accountNumber) {

        try {
            atmService.requestAtm(accountNumber);

            return ResponseEntity.ok(
                    Map.of("message", "ATM request submitted successfully")
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/has-pending-request")
    public ResponseEntity<Boolean> hasPendingRequest() {

        Long userId = getCurrentUserId();

        return ResponseEntity.ok(
                atmService.hasPendingAtmRequest(userId)
        );
    }


    // ================= WITHDRAW =================

 // ================= WITHDRAW =================

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody AtmWithdrawDto dto) {

        try {

            AtmReceipt receipt = atmService.withdraw(
                    dto.getCardNumber(),
                    dto.getPin(),
                    dto.getAmount()
            );

            return ResponseEntity.ok(receipt);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }



    // ================= RESET PIN =================

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/reset-pin")
    public ResponseEntity<?> resetPin(
            @RequestBody AtmPinResetDto dto) {

        try {

            atmService.resetPin(
                    dto.getAccountId(),
                    dto.getOldPin(),
                    dto.getNewPin()
            );

            return ResponseEntity.ok(
                    Map.of("message", "PIN reset successful")
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // ================= FORGOT PIN =================

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/forgot-pin/{cardNumber}")
    public ResponseEntity<?> forgotPin(
            @PathVariable String cardNumber) {

        try {

            String email =
                    atmService.sendPinResetOtp(cardNumber);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "OTP sent",
                            "email", maskEmail(email)
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // ================= RESET PIN WITH OTP =================

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/reset-pin-otp")
    public ResponseEntity<?> resetPinWithOtp(
            @RequestParam String cardNumber,
            @RequestParam int otp,
            @RequestParam String newPin) {

        try {

            atmService.resetPinWithOtp(cardNumber, otp, newPin);

            return ResponseEntity.ok(
                    Map.of("message", "PIN reset successful")
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // ================= ADMIN =================

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN')")
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<?> approveAtm(
            @PathVariable Long requestId) {

        atmService.approveAtm(requestId);

        return ResponseEntity.ok(
                Map.of("message", "ATM request approved")
        );
    }


    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN')")
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectAtm(
            @PathVariable Long requestId,
            @RequestBody AtmRejectDto dto) {

        atmService.rejectAtm(requestId, dto.getReason());

        return ResponseEntity.ok(
                Map.of("message", "ATM request rejected")
        );
    }


    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping("/requests/pending/count")
    public ResponseEntity<Long> pendingCount() {

        return ResponseEntity.ok(
                atmRequestRepo.countByStatus(Request.PENDING)
        );
    }


    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping("/requests/pending")
    public ResponseEntity<List<AtmRequestDetailDto>> pendingRequests() {

        List<AtmRequest> requests =
                atmRequestRepo.findByStatus(Request.PENDING);

        List<AtmRequestDetailDto> dtos =
                requests.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


    // ================= CARD DETAILS =================

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/card/{accountId}")
    public ResponseEntity<AtmCardDetailsDto> getCardDetails(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(
                atmService.getCardDetails(accountId)
        );
    }


    // ================= HELPERS =================

    private Long getCurrentUserId() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return user.getId();
    }


    private String maskEmail(String email) {

        String[] parts = email.split("@");

        if (parts.length == 2) {

            String name = parts[0];
            String domain = parts[1];

            if (name.length() > 2) {
                return name.substring(0, 2)
                        + "***@"
                        + domain;
            }
        }

        return email;
    }


    private AtmRequestDetailDto convertToDto(
            AtmRequest request) {

        AtmRequestDetailDto dto =
                new AtmRequestDetailDto();

        dto.setId(request.getId());
        dto.setStatus(request.getStatus().toString());
        dto.setRequestDate(request.getRequestDate());
        dto.setApprovedDate(request.getApprovedDate());
        dto.setRejectionReason(request.getRejectionReason());


        if (request.getRequestedBy() != null) {
            dto.setEmail(
                    request.getRequestedBy().getEmail()
            );
        }

        if (request.getAccount() != null) {

            dto.setAccountNumber(
                    request.getAccount().getAccountNumber()
            );

            if (request.getAccount().getUser() != null &&
                request.getAccount().getUser()
                        .getCustomerProfile() != null) {

                dto.setCustomerName(
                        request.getAccount()
                                .getUser()
                                .getCustomerProfile()
                                .getFullName()
                );
            }

            if (request.getAccount().getBranch() != null) {

                dto.setBranchName(
                        request.getAccount()
                                .getBranch()
                                .getBranchName()
                );

                dto.setBranchCode(
                        request.getAccount()
                                .getBranch()
                                .getBranchCode()
                );
            }
        }

        return dto;
    }
 // ================= RECEIPTS =================

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/receipts/{accountId}")
    public ResponseEntity<?> getReceipts(
            @PathVariable Long accountId) {

        try {

            return ResponseEntity.ok(
                    atmService.getReceipts(accountId)
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
