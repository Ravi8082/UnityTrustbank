package com.example.UnityTrustBank.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.CustomerProfile;

public interface CustomerProfileRepo extends JpaRepository<CustomerProfile, Long>{

}
