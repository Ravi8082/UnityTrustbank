package com.example.UnityTrustBank.dto;

import java.util.List;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.AccountRequest;
import com.example.UnityTrustBank.Entity.Branch;
import com.example.UnityTrustBank.Entity.CustomerProfile;
import com.example.UnityTrustBank.Entity.Role;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private Long id;
	private String email;
	private String password;
	private String mobile;
	private String age;
	private boolean active;
	@ManyToOne
	private Role role;
	@ManyToOne
	private Branch branch;
	@OneToOne(mappedBy = "user")
	private CustomerProfile customerProfile;
	@OneToMany(mappedBy = "user")
	private List<Account> accounts;

	@OneToMany(mappedBy = "approvedBy")
	private List<AccountRequest> approvedAccountRequests;

}
