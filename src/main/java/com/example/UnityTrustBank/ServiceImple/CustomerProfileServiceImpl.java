package com.example.UnityTrustBank.ServiceImple;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.CustomerProfile;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.CustomerProfileRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.CustomerProfileService;
import com.example.UnityTrustBank.dto.*;

@Service
public class CustomerProfileServiceImpl
        implements CustomerProfileService {

    @Autowired private UserRepo userRepo;
    @Autowired private CustomerProfileRepo profileRepo;

    @Override
    @Transactional
    public CustomerProfileResponseDto createProfile(
            Long userId,
            CustomerProfileCreateDto dto) {

        User current = getCurrentUser();

        if (!current.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized profile creation");
        }

        if (dto.getDob() == null || Period.between(dto.getDob(), LocalDate.now()).getYears() < 18) {
        	throw new RuntimeException("Applicant must be 18+"); }
        if (profileRepo.findByUser_Id(userId).isPresent()) {
            throw new RuntimeException("Customer profile already exists");
        }

        CustomerProfile profile = new CustomerProfile();
        profile.setUser(current);
        profile.setFullName(dto.getFullName());
        profile.setFatherName(dto.getFatherName());
        profile.setAddress(dto.getAddress());
        profile.setDob(dto.getDob());

        profile.setBranchVisitRequired(false);
        profile.setAadhaarVerified(false);
        profile.setPanVerified(false);

        return toDto(profileRepo.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponseDto getProfile(Long userId) {

        User current = getCurrentUser();

        if (!current.getId().equals(userId)
                && current.getRole().getRoleName() != AppRole.ROLE_ADMIN) {
            throw new RuntimeException("Unauthorized access");
        }

        CustomerProfile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return toDto(profile);
    }

    @Override
    @Transactional
    public void updateDocuments(Long userId, String aadhaar, String pan) {

        User current = getCurrentUser();

        if (!current.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized document update");
        }

        CustomerProfile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profile.isAadhaarVerified() || profile.isPanVerified()) {
            throw new RuntimeException("Verified documents cannot be changed");
        }

        if (aadhaar == null || aadhaar.length() != 12) {
            throw new RuntimeException("Invalid Aadhaar number");
        }

        if (pan == null || pan.length() != 10) {
            throw new RuntimeException("Invalid PAN number");
        }

        profile.setAadhaar(aadhaar);
        profile.setPan(pan);

        profileRepo.save(profile);
    }

    @Override
    @Transactional
    public void verifyAadhaar(Long userId) {

        User admin = getCurrentAdmin();

        CustomerProfile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setAadhaarVerified(true);
        profileRepo.save(profile);
    }

    @Override
    @Transactional
    public void verifyPan(Long userId) {

        User admin = getCurrentAdmin();

        CustomerProfile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setPanVerified(true);
        profileRepo.save(profile);
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private User getCurrentAdmin() {

        User user = getCurrentUser();

        if (user.getRole().getRoleName() != AppRole.ROLE_ADMIN) {
            throw new RuntimeException("Admin access required");
        }
        return user;
    }

    private CustomerProfileResponseDto toDto(CustomerProfile p) {

        return new CustomerProfileResponseDto(
                p.getId(),
                p.getFullName(),
                p.getFatherName(),
                p.getAddress(),
                p.getDob(),
                p.getAadhaar(),
                p.getPan(),
                p.isAadhaarVerified(),
                p.isPanVerified(),
                p.isBranchVisitRequired()
        );
    }
}
