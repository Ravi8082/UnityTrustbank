package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Service.BranchService;
import com.example.UnityTrustBank.dto.*;
@CrossOrigin("http://localhost:5175/")
@RestController
@RequestMapping("/branches")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class BranchController {

    @Autowired
    private BranchService service;
    @PostMapping("/create")
    public ResponseEntity<BranchResponseDto> create(
            @RequestBody BranchCreateDto dto) {

        return ResponseEntity.ok(service.createBranch(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BranchResponseDto> update(
            @PathVariable Long id,
            @RequestBody BranchUpdateDto dto) {

        return ResponseEntity.ok(service.updateBranch(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponseDto> get(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getBranch(id));
    }

    @GetMapping
    public ResponseEntity<List<BranchResponseDto>> all() {

        return ResponseEntity.ok(service.getAllBranches());
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id) {

        service.deactivateBranch(id);
        return ResponseEntity.ok("Branch deactivated");
    }
}
