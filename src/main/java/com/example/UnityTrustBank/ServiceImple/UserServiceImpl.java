package com.example.UnityTrustBank.ServiceImple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.UnityTrustBank.Entity.*;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.*;
import com.example.UnityTrustBank.Service.UserService;
import com.example.UnityTrustBank.dto.*;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Autowired private UserRepo userRepo;
    @Autowired private RoleRepo roleRepo;
    @Autowired private BranchRepo branchRepo;
    

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
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

        return toDto(userRepo.save(user));
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


}
