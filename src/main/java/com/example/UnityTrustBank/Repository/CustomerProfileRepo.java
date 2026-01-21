package com.example.UnityTrustBank.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.CustomerProfile;

public interface CustomerProfileRepo
        extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByUser_Id(Long userId);
}
