package com.example.UnityTrustBank.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.Account;

public interface AccountRepo extends JpaRepository<Account, Long>{

	boolean existsByUser_Id(Long userid);

}
