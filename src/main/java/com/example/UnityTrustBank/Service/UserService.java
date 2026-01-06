package com.example.UnityTrustBank.Service;

import java.util.List;


import com.example.UnityTrustBank.dto.UserDto;
public interface UserService {
	
    UserDto createCustomer(UserDto user);

    UserDto createManager(UserDto user);

    UserDto getUserById(Long id);

    List<UserDto> getAllCustomer();

    List<UserDto> getAllManagers();

    void deactivateUser(Long userId);
}
