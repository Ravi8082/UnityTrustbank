package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.Entity.AccountRequest;

public interface AccountRequestService {
	
	void createAccountRequest(Long userid, long branchId, String accountType);
	void approveAccountRequest(Long requestId, Long managerId);
	void rejectAccountRequest(Long requestId, Long managerId, String reason);
	 List<AccountRequest> getPendingRequestsByBranch(Long branchId);
	 List<AccountRequest> getRequestsByUser(Long userId);
}
