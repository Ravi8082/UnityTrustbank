package com.example.UnityTrustBank.ServiceImple;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.UnityTrustBank.Entity.Role;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.RoleRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.UserService;
import com.example.UnityTrustBank.dto.UserDto;
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDto createCustomer(UserDto userDto) {
    	if(userRepo.findByEmail(userDto.getEmail())) {
    		throw new RuntimeException("Emaily already Present");
    	}

        User user = modelMapper.map(userDto, User.class);
        if(!user.getEmail().equals(userDto.getEmail())) {
        	
        }

        Role role = roleRepo.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        user.setRole(role);
        user.setActive(true);

        User savedUser = userRepo.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto createManager(UserDto userDto) {
    	if(userRepo.findByEmail(userDto.getEmail())) {
    		throw new RuntimeException("Emaily already Present");
    	}

        User user = modelMapper.map(userDto, User.class);

        Role role = roleRepo.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        if (user.getBranch() == null) {
            throw new RuntimeException("Manager must be assigned to a branch");
        }

        user.setRole(role);
        user.setActive(true);

        User savedUser = userRepo.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserById(Long id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> getAllCustomer() {

        List<User> users = userRepo.findByRole_RoleName(AppRole.ROLE_USER);

        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    public List<UserDto> getAllManagers() {

        List<User> users = userRepo.findByRole_RoleName(AppRole.ROLE_ADMIN);

        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    public void deactivateUser(Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);
        userRepo.save(user);
    }
}
