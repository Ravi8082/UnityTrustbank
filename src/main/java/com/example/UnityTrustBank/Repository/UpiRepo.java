package com.example.UnityTrustBank.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.UpiAccount;

public interface UpiRepo extends JpaRepository<UpiAccount, Long> {

    Optional<UpiAccount> findByVpa(String vpa);

    boolean existsByVpa(String vpa);

    boolean existsByAccount_Id(Long accountId);

    Optional<UpiAccount> findByAccount_Id(Long accountId);
}
