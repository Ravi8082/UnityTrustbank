package com.example.UnityTrustBank.Service;

import java.util.List;

import com.example.UnityTrustBank.dto.PasswordResetDto;
import com.example.UnityTrustBank.dto.UserDto;
import com.example.UnityTrustBank.dto.UserResponseDto;

public interface UserService {

    UserResponseDto createManager(UserDto dto);

    UserResponseDto getUser(Long id);

    List<UserResponseDto> getAllCustomers();

    void deactivateUser(Long userId);
    void resetPassword(PasswordResetDto dto);
}
