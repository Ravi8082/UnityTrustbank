package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.dto.AdminCreateDto;
import com.example.UnityTrustBank.dto.AdminUserDto;
import com.example.UnityTrustBank.dto.PasswordResetDto;
import com.example.UnityTrustBank.dto.UserDto;
import com.example.UnityTrustBank.dto.UserResponseDto;

public interface UserService {

    UserResponseDto createManager(UserDto dto);
    
    UserResponseDto createAdminWithProfile(AdminCreateDto dto);

    UserResponseDto getUser(Long id);

    List<UserResponseDto> getAllCustomers();
    
    List<UserResponseDto> getAllUsers();

    List<UserResponseDto> getAllUsersInBranch(Long branchId);
    
    List<UserResponseDto> getAllCustomersInBranch(Long branchId);
    
    void deactivateUser(Long userId);
    
    void deactivateUserInBranch(Long userId, Long branchId);
    
    void resetPassword(PasswordResetDto dto);
    
    List<AdminUserDto> getAllUsersWithDetails();

}