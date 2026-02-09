package com.example.UnityTrustBank.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.Branch;

public interface BranchRepo extends JpaRepository<Branch, Long> {

    boolean existsByBranchCode(String branchCode);
    boolean existsByIfscCode(String ifscCode);
    boolean existsByAccountPrefix(String accountPrefix);
    List<Branch> findByActiveTrue();
     Optional<Branch> findByBranchCode(String branchCode);
}
