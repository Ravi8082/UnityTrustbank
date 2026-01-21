package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.dto.*;

public interface BranchService {

    BranchResponseDto createBranch(BranchCreateDto dto);

    BranchResponseDto updateBranch(Long id, BranchUpdateDto dto);

    BranchResponseDto getBranch(Long id);

    List<BranchResponseDto> getAllBranches();

    void deactivateBranch(Long id);
}
