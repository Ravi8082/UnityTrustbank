package com.example.UnityTrustBank.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole_RoleName(AppRole roleName);
    long countByBranch_Id(Long branchId);

	Optional<User> findByBranch_Id(Long branchId);

}
