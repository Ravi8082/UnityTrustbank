package com.example.UnityTrustBank.ServiceImple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.*;
import com.example.UnityTrustBank.Repository.*;
import com.example.UnityTrustBank.Service.AccountSequenceService;

@Service
public class AccountSequenceServiceImpl
        implements AccountSequenceService {

    @Autowired
    private AccountSequenceRepo sequenceRepo;

    @Autowired
    private BranchRepo branchRepo;
    @Override
    @Transactional
    public String generateAccountNumber(Long branchId) {

   
        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (!branch.isActive()) {
            throw new RuntimeException("Branch inactive");
        }

    
        AccountSequence seq = sequenceRepo
                .findByBranchForUpdate(branchId)
                .orElseGet(() -> {
                    AccountSequence s = new AccountSequence();
                    s.setBranch(branch);
                    s.setCurrentValue(0L);
                    return sequenceRepo.saveAndFlush(s);
                });

      
        long next = seq.getCurrentValue() + 1;

 
        if (next > 99999) {
            throw new RuntimeException(
                "Account number limit exceeded for branch " +
                branch.getBranchCode()
            );
        }

        seq.setCurrentValue(next);
        sequenceRepo.save(seq);

     
        return branch.getAccountPrefix()
                + String.format("%05d", next);
    }

    @Override
    @Transactional(readOnly = true)
    public String previewNextAccountNumber(Long branchId) {

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (!branch.isActive()) {
            throw new RuntimeException("Branch inactive");
        }

        long current = sequenceRepo
                .findByBranch_Id(branchId)
                .map(AccountSequence::getCurrentValue) 
                .orElse(0L);
        long next = current + 1;

        if (next > 99999) {
            throw new RuntimeException(
                "Account number limit exceeded for branch " +
                branch.getBranchCode()
            );
        }

        return branch.getAccountPrefix()
                + String.format("%05d", next);
    }
}
