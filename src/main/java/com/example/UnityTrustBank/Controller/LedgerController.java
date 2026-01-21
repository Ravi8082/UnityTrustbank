package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Entity.LedgerEntry;
import com.example.UnityTrustBank.Repository.LedgerRepo;

@RestController
@RequestMapping("/ledger")
@CrossOrigin("http://localhost:5175/")
public class LedgerController {

    @Autowired
    private LedgerRepo ledgerRepo;

    // USER can see only his account ledger
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LedgerEntry>> getLedgerForAccount(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(
                ledgerRepo.findByAccount_IdOrderByCreatedAtDesc(accountId)
        );
    }

    // ADMIN can search by reference number
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/reference/{ref}")
    public ResponseEntity<List<LedgerEntry>> getByReference(
            @PathVariable String ref) {

        return ResponseEntity.ok(
                ledgerRepo.findByReferenceNo(ref)
        );
    }
}
