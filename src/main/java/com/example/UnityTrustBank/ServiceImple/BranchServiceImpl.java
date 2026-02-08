package com.example.UnityTrustBank.ServiceImple;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.Branch;
import com.example.UnityTrustBank.Enum.AccountStatus;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.BranchRepo;
import com.example.UnityTrustBank.Service.BranchService;
import com.example.UnityTrustBank.dto.*;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepo branchRepo;
    @Autowired
    private AccountRepo accountRepo;

    @Override
    @Transactional
    public BranchResponseDto createBranch(BranchCreateDto dto) {

        if (isBlank(dto.getBranchName())
                || isBlank(dto.getBranchCode())
                || isBlank(dto.getIfscCode())
                || isBlank(dto.getAccountPrefix())
                || isBlank(dto.getCity())
                || isBlank(dto.getState())) {
            throw new RuntimeException("All branch fields are mandatory");
        }

        if (branchRepo.existsByBranchCode(dto.getBranchCode())) {
            throw new RuntimeException("Branch code already exists");
        }
        if (branchRepo.existsByIfscCode(dto.getIfscCode())) {
            throw new RuntimeException("IFSC already exists");
        }
        if (branchRepo.existsByAccountPrefix(dto.getAccountPrefix())) {
            throw new RuntimeException("Account prefix already exists");
        }

        Branch branch = new Branch();
        branch.setBranchName(dto.getBranchName().trim());
        branch.setBranchCode(dto.getBranchCode().trim());
        branch.setIfscCode(dto.getIfscCode().trim());
        branch.setAccountPrefix(dto.getAccountPrefix().trim());
        branch.setCity(dto.getCity().trim());
        branch.setState(dto.getState().trim());
        branch.setActive(true);

        return toDto(branchRepo.save(branch));
    }

    @Override
    @Transactional
    public BranchResponseDto updateBranch(Long id, BranchUpdateDto dto) {

        Branch branch = branchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (!branch.isActive()) {
            throw new RuntimeException("Inactive branch cannot be updated");
        }

        if (isBlank(dto.getCity()) || isBlank(dto.getState())) {
            throw new RuntimeException("City and State are required");
        }

        branch.setCity(dto.getCity().trim());
        branch.setState(dto.getState().trim());

        return toDto(branchRepo.save(branch));
    }


    @Override
    @Transactional(readOnly = true)
    public BranchResponseDto getBranch(Long id) {

        return toDto(
            branchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getAllBranches() {

        return branchRepo.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }


    @Override
    @Transactional
    public void deactivateBranch(Long id) {

        Branch branch = branchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (!branch.isActive()) {
            throw new RuntimeException("Branch already inactive");
        }


        long activeAccounts =
            accountRepo.countByBranch_IdAndStatus(
                id, AccountStatus.ACTIVE);

        if (activeAccounts > 0) {
        	throw new IllegalStateException(
        			   "This branch has active accounts. Please close them first."
        			);

        }

        branch.setActive(false);
        branchRepo.save(branch);
    }

    private BranchResponseDto toDto(Branch b) {

        return new BranchResponseDto(
                b.getId(),
                b.getBranchName(),
                b.getBranchCode(),
                b.getIfscCode(),
                b.getAccountPrefix(),
                b.getCity(),
                b.getState(),
                b.isActive()
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    @Transactional
    public void migrateBranch(Long sourceBranchId, Long targetBranchId) {
        if (sourceBranchId.equals(targetBranchId)) {
            throw new RuntimeException("Source and target branch cannot be the same");
        }

        Branch source = branchRepo.findById(sourceBranchId)
                .orElseThrow(() -> new RuntimeException("Source branch not found"));
        
        Branch target = branchRepo.findById(targetBranchId)
                .orElseThrow(() -> new RuntimeException("Target branch not found"));

        if (!target.isActive()) {
            throw new RuntimeException("Target branch is inactive");
        }

        List<Account> accounts = accountRepo.findByBranch_Id(sourceBranchId);
        
        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found in source branch to migrate");
        }

        for (Account account : accounts) {
            account.setBranch(target);
            // Note: In real scenarios, you might also want to update the IFSC code 
            // if it's stored at the account level, or notify the user.
        }

        accountRepo.saveAll(accounts);
    }
    @Override
    public List<BranchResponseDto> getActiveBranches() {
        return branchRepo.findByActiveTrue()
            .stream()
            .map(this::toDto)  // Changed from toResponseDto to toDto
            .collect(Collectors.toList());
    }

}
