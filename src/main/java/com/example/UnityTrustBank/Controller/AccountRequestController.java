package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.UnityTrustBank.Entity.AccountRequest;
import com.example.UnityTrustBank.Service.AccountRequestService;

@RestController
@RequestMapping("/api/account-requests")
public class AccountRequestController {
	@Autowired
	private AccountRequestService accountRequestService;
	
	@PostMapping
	public ResponseEntity<String> applyForAccount(@RequestParam Long userId, @RequestParam Long branchId, 
			@RequestParam String accountType) {
		accountRequestService.createAccountRequest(userId, branchId, accountType);
				return ResponseEntity.ok("Account request submitted successfully");
		
	}
	
	@PutMapping("/{id}/approve")
	public ResponseEntity<String> approveAccountRequest(@PathVariable Long requestId,
			@RequestParam Long managerId){
		accountRequestService.approveAccountRequest(requestId, managerId);
				return ResponseEntity.ok("Account request approved successfully");
		
	}
	
	@PutMapping("/{id}/rejected")
	public ResponseEntity<String> rejectAccountRequest(@PathVariable Long requestId,
			@RequestParam Long managerId, @RequestBody String reason){
		accountRequestService.rejectAccountRequest(requestId, managerId, reason);
				return ResponseEntity.ok("Account Request Rejected");
		
	}
	
	@GetMapping("/{id}/pending")
	public ResponseEntity<List<AccountRequest>> pendingAccountRequest(@PathVariable Long id){
	  List<AccountRequest> reuest =	accountRequestService.getPendingRequestsByBranch(id);
		return ResponseEntity.ok(reuest);
		
	}
	 
	    @GetMapping("/user/{id}")
	    public ResponseEntity<List<AccountRequest>> getRequestsByUser(
	            @PathVariable Long id) {

	        List<AccountRequest> requests =
	                accountRequestService.getRequestsByUser(id);
	        return ResponseEntity.ok(requests);
	    }
	

}
