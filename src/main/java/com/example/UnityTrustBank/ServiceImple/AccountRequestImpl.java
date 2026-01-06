package com.example.UnityTrustBank.ServiceImple;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.UnityTrustBank.Entity.AccountRequest;
import com.example.UnityTrustBank.Entity.CustomerProfile;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.Repository.AccountRepo;
import com.example.UnityTrustBank.Repository.AccountRequestRepo;
import com.example.UnityTrustBank.Repository.BranchRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.AccountRequestService;

public class AccountRequestImpl implements AccountRequestService{
	@Autowired
	private AccountRequestRepo accountRequestRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private BranchRepo branchRepo;
	@Autowired
	private AccountRepo accountRepo;
	@Autowired
	private ModelMapper modelMapper;
	@Override
	public void createAccountRequest(Long userid, long branchId, String accountType) {
		User user = userRepo.findById(userid).orElseThrow(()->
		new RuntimeException("User not found"));
	
	CustomerProfile profile = user.getCustomerProfile();
	if(profile == null || profile.getDob()==null) {
		throw new RuntimeException("Customer profile or DOB missing");
	}
	
	int age = Period.between(profile.getDob(), LocalDate.now()).getYears();
	if(age < 18) {
		profile.setBranchVisitRequired(true);
		throw new RuntimeException("Customer below 18. Branch visit required for account opening");
	}
	if(profile.getAadhaar() == null || profile.getPan()== null) {
		throw new RuntimeException("Aadhar and Pan are madatory");
	}
	if(accountRepo.existsByUser_Id(userid)) {
		throw new RuntimeException("Account already exists for this user");
		
	}
	if(accountRequestRepo.existsByUser_IdAndStatus(userid, Request.PENDING)) {
            throw new RuntimeException("Account request already pending");
        }
	}
	@Override
	public void approveAccountRequest(Long requestId, Long managerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rejectAccountRequest(Long requestId, Long managerId, String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AccountRequest> getPendingRequestsByBranch(Long branchId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AccountRequest> getRequestsByUser(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
