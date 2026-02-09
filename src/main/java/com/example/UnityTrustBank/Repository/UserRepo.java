package com.example.UnityTrustBank.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByMobile(String mobile);

    List<User> findByRole_RoleName(AppRole roleName);
    List<User> findByBranch_IdAndRole_RoleName(Long branchId, AppRole roleName);
    long countByBranch_Id(Long branchId);
    Optional<User> findByIdAndBranch_Id(Long id, Long branchId);

	List<User> findByBranch_Id(Long branchId);

	Long countByBranch_IdIsNotNull();
	@Query("""
			SELECT DISTINCT u FROM User u
			LEFT JOIN FETCH u.customerProfile
			LEFT JOIN FETCH u.accounts
			LEFT JOIN FETCH u.branch
			""")
			List<User> findAllWithDetails();


}
