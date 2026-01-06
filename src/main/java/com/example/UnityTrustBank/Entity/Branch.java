package com.example.UnityTrustBank.Entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Branch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String branchName;
	private String branchCode;
	private String ifscCode;
	private String accountPrefix;
	private String City;
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
