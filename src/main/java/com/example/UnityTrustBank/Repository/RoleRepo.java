package com.example.UnityTrustBank.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.UnityTrustBank.Entity.Role;
import com.example.UnityTrustBank.Enum.AppRole;

public interface RoleRepo  extends JpaRepository<Role, Long>{
	Optional<Role> findByRoleName(AppRole appRole);
}
