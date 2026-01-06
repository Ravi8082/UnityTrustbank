package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.dto.BranchDto;
import com.example.UnityTrustBank.dto.BranchUpdateDto;

public interface BranchService {
	BranchDto createBranch(BranchDto branchDto);
	
	BranchDto getBranchById(Long id);
	
	List<BranchDto> getAllBranch();
	
	void deactivateBranch(Long id);

	BranchDto updateBranch(Long id, BranchUpdateDto branchUpdateDto);

}
