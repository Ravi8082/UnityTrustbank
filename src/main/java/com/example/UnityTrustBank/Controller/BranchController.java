package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Service.BranchService;
import com.example.UnityTrustBank.dto.*;

@RestController
@RequestMapping("/branches")
// Remove the class-level @PreAuthorize annotation
public class BranchController {

    @Autowired
    private BranchService service;
    
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<BranchResponseDto> create(
            @RequestBody BranchCreateDto dto) {

        return ResponseEntity.ok(service.createBranch(dto));
    }
    
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<BranchResponseDto> update(
            @PathVariable Long id,
            @RequestBody BranchUpdateDto dto) {

        return ResponseEntity.ok(service.updateBranch(id, dto));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<BranchResponseDto> get(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getBranch(id));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<BranchResponseDto>> all() {

        return ResponseEntity.ok(service.getAllBranches());
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id) {

        service.deactivateBranch(id);
        return ResponseEntity.ok("Branch deactivated");
    }
    
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/migrate")
    public ResponseEntity<String> migrate(
            @RequestParam Long sourceId,
            @RequestParam Long targetId) {

        service.migrateBranch(sourceId, targetId);
        return ResponseEntity.ok("Accounts migrated successfully from branch " + sourceId + " to " + targetId);
    }

    @GetMapping("/public/active")
    public ResponseEntity<List<BranchResponseDto>> getActiveBranches() {
        return ResponseEntity.ok(service.getActiveBranches());
    }
}