package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.UnityTrustBank.Entity.Branch;
import com.example.UnityTrustBank.Service.BranchService;
import com.example.UnityTrustBank.dto.BranchDto;
import com.example.UnityTrustBank.dto.BranchUpdateDto;

@RestController
@RequestMapping("/branch")
public class BranchController {
	@Autowired
	private BranchService branchService;
	
	@PostMapping("/create")
	public ResponseEntity<BranchDto> createBranch(@RequestBody BranchDto branchDto){
		BranchDto savedbranchDto = branchService.createBranch(branchDto);
		return new ResponseEntity<>(savedbranchDto,HttpStatus.CREATED);
		
	}
	
	@GetMapping("/all")
	public ResponseEntity<List<BranchDto>> fetchAllBranch(){
		return ResponseEntity.ok(branchService.getAllBranch());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<BranchDto> getById(@PathVariable Long id){
		return ResponseEntity.ok(branchService.getBranchById(id));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<BranchDto> updateBranch(@PathVariable Long id, BranchUpdateDto branchDto){
		BranchDto branch = branchService.updateBranch(id,branchDto );
		return ResponseEntity.ok(branch);
		
	}
	 @PutMapping("/{id}/deactivate")
	    public ResponseEntity<String> deactivateBranch(@PathVariable Long id) {
	        branchService.deactivateBranch(id);
	        return ResponseEntity.ok("Branch deactivated successfully");
	    }

}
