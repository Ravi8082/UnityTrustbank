package com.example.UnityTrustBank.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.AccountRequest;

public interface AccountRequestRepo  extends JpaRepository<AccountRequest, Long>{

}
