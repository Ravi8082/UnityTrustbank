package com.example.UnityTrustBank.dto;

import java.util.List;

import com.example.UnityTrustBank.Entity.Account;
import com.example.UnityTrustBank.Entity.AccountSequence;
import com.example.UnityTrustBank.Entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchUpdateDto {
	private Long id;
	private String branchName;
    private String city;
    private String state;
	
	@OneToMany(mappedBy = "branch")
	@JsonBackReference
	private List<User> users;

	@OneToMany(mappedBy = "branch")
	@JsonBackReference
	private List<Account> accounts;

	@OneToOne(mappedBy = "branch")
	@JsonBackReference
	private AccountSequence accountSequence;

}
