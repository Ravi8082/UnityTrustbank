package com.example.UnityTrustBank.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Service.AccountSequenceService;

@RestController
@RequestMapping("/sequence")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AccountSequenceController {

    @Autowired
    private AccountSequenceService service;

    @GetMapping("/branch/{branchId}/preview")
    public String preview(@PathVariable Long branchId) {
        return service.previewNextAccountNumber(branchId);
    }
}
