package com.example.UnityTrustBank.ServiceImple;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.UnityTrustBank.Entity.*;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.*;
import com.example.UnityTrustBank.Service.CustomerProfileService;
import com.example.UnityTrustBank.Service.UserService;
import com.example.UnityTrustBank.dto.CustomerProfileCreateDto;
import com.example.UnityTrustBank.dto.PasswordResetDto;
import com.example.UnityTrustBank.dto.UserDto;
import com.example.UnityTrustBank.dto.UserResponseDto;
import com.example.UnityTrustBank.dto.AdminCreateDto;
import com.example.UnityTrustBank.dto.AdminUserDto;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private UserRepo userRepo;
    private RoleRepo roleRepo;
    private BranchRepo branchRepo;
    private CustomerProfileService customerProfileService;

    // Constructor injection for password encoder
    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Field injection for repositories to avoid circular dependency issues
    @Autowired
    public void setUserRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Autowired
    public void setRoleRepo(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Autowired
    public void setBranchRepo(BranchRepo branchRepo) {
        this.branchRepo = branchRepo;
    }

    @Autowired
    public void setCustomerProfileService(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @Override
    public UserResponseDto createManager(UserDto dto) {

        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepo.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        Branch branch = branchRepo.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setMobile(dto.getMobile());
        user.setRole(role);
        user.setBranch(branch);
        user.setActive(true);

        User savedUser = userRepo.save(user);
        
        // Create customer profile for the admin with personal details
        CustomerProfileCreateDto profileDto = new CustomerProfileCreateDto();
        profileDto.setFullName(dto.getEmail().split("@")[0]); // Use email username as default name
        profileDto.setFatherName("");
        profileDto.setAddress("");
        profileDto.setDob(null);
        // Note: CustomerProfileCreateDto doesn't have gender, aadhaar, pan, or image path fields
        
        customerProfileService.createProfile(savedUser.getId(), profileDto);
        
        return toDto(savedUser);
    }
    
    @Override
    @Transactional
    public UserResponseDto createAdminWithProfile(AdminCreateDto dto) {
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepo.existsByMobile(dto.getMobile())) {
            throw new RuntimeException("Mobile number already exists");
        }

        Role role = roleRepo.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        Branch branch = branchRepo.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setMobile(dto.getMobile());
        user.setRole(role);
        user.setBranch(branch);
        user.setActive(true);

        User savedUser = userRepo.save(user);
        
        // Create customer profile for the admin with provided details
        CustomerProfileCreateDto profileDto = new CustomerProfileCreateDto();
        profileDto.setFullName(dto.getFullName());
        profileDto.setFatherName(dto.getFatherName());
        profileDto.setAddress(dto.getAddress());
        profileDto.setDob(null); // Assuming no DOB in admin creation
        // Note: CustomerProfileCreateDto doesn't have gender, aadhaar, pan, or image path fields
        
        customerProfileService.createProfile(savedUser.getId(), profileDto);
        
        return toDto(savedUser);
    }

    @Override
    public UserResponseDto getUser(Long id) {
        return toDto(
            userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"))
        );
    }

    @Override
    public List<UserResponseDto> getAllCustomers() {

        return userRepo.findByRole_RoleName(AppRole.ROLE_USER)
                .stream()
                .map(this::toDto)
                .toList();
    }
    
    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void deactivateUser(Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);
        userRepo.save(user);
    }

    private UserResponseDto toDto(User u) {
        return new UserResponseDto(
                u.getId(),
                u.getEmail(),
                u.getMobile(),
                u.isActive(),
                u.getRole().getRoleName().name()
        );
    }
    
    @Transactional
    @Override
    public void resetPassword(PasswordResetDto dto) {

        User user = getCurrentUser();

        if (!user.isPasswordResetRequired()) {
            throw new RuntimeException("Password reset not required");
        }

        if (!passwordEncoder.matches(
                dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (dto.getNewPassword().length() < 8) {
            throw new RuntimeException("Password too weak");
        }

        user.setPassword(
            passwordEncoder.encode(dto.getNewPassword())
        );
        user.setPasswordResetRequired(false);

        userRepo.save(user);
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

	@Override
	public List<UserResponseDto> getAllUsersInBranch(Long branchId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserResponseDto> getAllCustomersInBranch(Long branchId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deactivateUserInBranch(Long userId, Long branchId) {
		// TODO Auto-generated method stub
		
	}
	@Transactional(readOnly = true)
	@Override
	public List<AdminUserDto> getAllUsersWithDetails() {

	    return userRepo.findAllWithDetails()
	        .stream()
	        .map(u -> {

	            String fullName =
	                u.getCustomerProfile() != null
	                ? u.getCustomerProfile().getFullName()
	                : "N/A";

	            String address =
	                u.getCustomerProfile() != null
	                ? u.getCustomerProfile().getAddress()
	                : "N/A";

	            String branchName =
	                u.getBranch() != null
	                ? u.getBranch().getBranchName()
	                : "N/A";

	            List<String> accounts =
	                (u.getAccounts() == null)
	                ? List.of()
	                : u.getAccounts()
	                    .stream()
	                    .map(Account::getAccountNumber)
	                    .toList();

	            return new AdminUserDto(
	                u.getId(),
	                u.getEmail(),
	                u.getMobile(),
	                u.getRole().getRoleName().name(),
	                u.isActive(),
	                fullName,
	                address,
	                branchName,
	                accounts
	            );
	        })
	        .toList();
	}



	
}