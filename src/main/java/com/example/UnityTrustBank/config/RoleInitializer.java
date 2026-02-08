package com.example.UnityTrustBank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.UnityTrustBank.Entity.Branch;
import com.example.UnityTrustBank.Entity.Role;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.BranchRepo;
import com.example.UnityTrustBank.Repository.RoleRepo;
import com.example.UnityTrustBank.Repository.UserRepo;

@Component
public class RoleInitializer implements ApplicationRunner {

    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private RoleRepo roleRepo;
    
    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // Create roles if they don't exist (with safety check)
            try {
                roleRepo.findByRoleName(AppRole.ROLE_USER)
                        .orElseGet(() -> {
                            Role userRole = new Role();
                            userRole.setRoleName(AppRole.ROLE_USER);
                            return roleRepo.save(userRole);
                        });
            } catch (Exception e) {
                System.out.println("Could not initialize USER role, possibly table not ready: " + e.getMessage());
            }
            
            try {
                roleRepo.findByRoleName(AppRole.ROLE_ADMIN)
                        .orElseGet(() -> {
                            Role adminRole = new Role();
                            adminRole.setRoleName(AppRole.ROLE_ADMIN);
                            return roleRepo.save(adminRole);
                        });
            } catch (Exception e) {
                System.out.println("Could not initialize ADMIN role, possibly table not ready: " + e.getMessage());
            }
            
            try {
                roleRepo.findByRoleName(AppRole.ROLE_MANAGER)
                        .orElseGet(() -> {
                            Role managerRole = new Role();
                            managerRole.setRoleName(AppRole.ROLE_MANAGER);
                            return roleRepo.save(managerRole);
                        });
            } catch (Exception e) {
                System.out.println("Could not initialize MANAGER role, possibly table not ready: " + e.getMessage());
            }

            // Create super admin if it doesn't exist
            try {
                userRepo.findByEmail("manager@utb.com")
                        .orElseGet(() -> {
                            User manager = new User();
                            manager.setEmail("manager@utb.com");
                            manager.setPassword(passwordEncoder.encode("Manager@123"));
                            manager.setMobile("9999999999");
                            manager.setActive(true);
                            
                            Role managerRole = roleRepo.findByRoleName(AppRole.ROLE_MANAGER)
                                    .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found"));
                            manager.setRole(managerRole);
                            
                            // Set a default branch - use existing branch or create a default one
                            Branch defaultBranch = branchRepo.findById(1L).orElseGet(() -> {
                                Branch branch = new Branch();
                                branch.setId(1L);
                                branch.setBranchName("Main Branch");
                                branch.setBranchCode("MB001");
                                branch.setIfscCode("UTBI0000001");
                                branch.setAccountPrefix("MB001");
                                branch.setCity("Default City");
                                branch.setState("Default State");
                                branch.setActive(true);
                                return branchRepo.save(branch);
                            });
                            
                            manager.setBranch(defaultBranch);
                            
                            return userRepo.save(manager);
                        });
            } catch (Exception e) {
                System.out.println("Could not initialize super admin, possibly table not ready: " + e.getMessage());
            }

            // Update existing admin password if needed
            try {
                userRepo.findByEmail("admin@utb.com")
                        .ifPresent(user -> {
                            user.setPassword(passwordEncoder.encode("Admin@123"));
                            userRepo.save(user);
                        });
            } catch (Exception e) {
                System.out.println("Could not update admin password, possibly table not ready: " + e.getMessage());
            }

            System.out.println("ROLES AND SUPER ADMIN INITIALIZATION COMPLETED");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}