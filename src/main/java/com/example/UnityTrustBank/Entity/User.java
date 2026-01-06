package com.example.UnityTrustBank.Entity;

import java.util.List;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 2, nullable = false) 
	@NotBlank(message="please enter email")
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
