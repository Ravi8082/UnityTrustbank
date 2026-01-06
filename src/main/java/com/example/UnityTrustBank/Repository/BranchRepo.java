package com.example.UnityTrustBank.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.Branch;

public interface BranchRepo extends JpaRepository<Branch, Long> {

}
