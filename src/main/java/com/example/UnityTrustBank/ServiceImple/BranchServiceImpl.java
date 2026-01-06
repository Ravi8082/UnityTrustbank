package com.example.UnityTrustBank.ServiceImple;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.UnityTrustBank.Entity.Branch;
import com.example.UnityTrustBank.Repository.BranchRepo;
import com.example.UnityTrustBank.Service.BranchService;
import com.example.UnityTrustBank.dto.BranchDto;
import com.example.UnityTrustBank.dto.BranchUpdateDto;
@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BranchDto createBranch(BranchDto branchDto) {

        if (branchRepo.existsByIfscCode(branchDto.getIfscCode())) {
            throw new RuntimeException("IFSC code already exists");
        }

        if (branchRepo.existsByAccountPrefix(branchDto.getAccountPrefix())) {
            throw new RuntimeException("Account prefix already exists");
        }

        Branch branch = modelMapper.map(branchDto, Branch.class);
        branch.setActive(true);

        Branch savedBranch = branchRepo.save(branch);
        return modelMapper.map(savedBranch, BranchDto.class);
    }

    @Override
    public BranchDto getBranchById(Long id) {
        Branch branch = branchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return modelMapper.map(branch, BranchDto.class);
    }

    @Override
    public List<BranchDto> getAllBranch() {
        return branchRepo.findAll()
                .stream()
                .map(branch -> modelMapper.map(branch, BranchDto.class))
                .toList();
    }

    @Override
    public BranchDto updateBranch(Long id, BranchUpdateDto dto) {

        Branch branch = branchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        branch.setBranchName(dto.getBranchName());
        branch.setCity(dto.getCity());
        branch.setState(dto.getState());

        Branch updated = branchRepo.save(branch);
        return modelMapper.map(updated, BranchDto.class);
    }

    @Override
    public void deactivateBranch(Long id) {
        Branch branch = branchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        branch.setActive(false);
        branchRepo.save(branch);
    }
}
