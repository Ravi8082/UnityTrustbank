package com.example.UnityTrustBank.ServiceImple;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.UnityTrustBank.Entity.*;
import com.example.UnityTrustBank.Enum.*;
import com.example.UnityTrustBank.Repository.*;
import com.example.UnityTrustBank.Service.CustomerProfileService;
import com.example.UnityTrustBank.dto.*;

@Service
public class CustomerProfileServiceImpl implements CustomerProfileService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CustomerProfileRepo profileRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountApplicationRepo appRepo;



    // ================= CREATE PROFILE =================

    @Override
    @Transactional
    public CustomerProfileResponseDto createProfile(
            Long userId,
            CustomerProfileCreateDto dto) {

        User current = getCurrentUser();

        if (!current.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized profile creation");
        }

        if (dto.getDob() == null ||
            Period.between(dto.getDob(), LocalDate.now()).getYears() < 18) {
            throw new RuntimeException("Applicant must be 18+");
        }

        if (profileRepo.findByUser_Id(userId).isPresent()) {
            throw new RuntimeException("Profile already exists");
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



    // ================= GET PROFILE =================

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



    // ================= UPDATE DOCUMENTS =================

    @Override
    @Transactional
    public void updateDocuments(Long userId, String aadhaar, String pan) {

        User current = getCurrentUser();

        if (!current.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized update");
        }

        CustomerProfile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profile.isAadhaarVerified() || profile.isPanVerified()) {
            throw new RuntimeException("Verified documents locked");
        }

        if (aadhaar == null || aadhaar.length() != 12)
            throw new RuntimeException("Invalid Aadhaar");

        if (pan == null || pan.length() != 10)
            throw new RuntimeException("Invalid PAN");

        profile.setAadhaar(aadhaar);
        profile.setPan(pan);

        profileRepo.save(profile);
    }



    // ================= VERIFY AADHAAR =================

    @Override
    @Transactional
    public void verifyAadhaar(Long applicationId) {

        User admin = getCurrentAdmin();

        AccountApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // ✅ Get user using email
        User user = userRepo.findByEmail(app.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = profileRepo.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (!user.getBranch().getId().equals(admin.getBranch().getId())) {
            throw new RuntimeException("Unauthorized branch access");
        }

        profile.setAadhaarVerified(true);
        profileRepo.save(profile);

        checkAndActivateAccount(profile);
    }



    // ================= VERIFY PAN =================

    @Override
    @Transactional
    public void verifyPan(Long applicationId) {

        User admin = getCurrentAdmin();

        AccountApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // ✅ Get user using email
        User user = userRepo.findByEmail(app.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = profileRepo.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (!user.getBranch().getId().equals(admin.getBranch().getId())) {
            throw new RuntimeException("Unauthorized branch access");
        }

        profile.setPanVerified(true);
        profileRepo.save(profile);

        checkAndActivateAccount(profile);
    }



    // ================= HELPERS =================

    private User getCurrentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    private User getCurrentAdmin() {

        User user = getCurrentUser();

        if (user.getRole().getRoleName() != AppRole.ROLE_ADMIN) {
            throw new RuntimeException("Admin only");
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



    // ================= AUTO ACTIVATE =================

    private void checkAndActivateAccount(CustomerProfile profile) {

        if (profile.isAadhaarVerified() && profile.isPanVerified()) {

            accountRepo.findByUser_Id(profile.getUser().getId())
                    .forEach(acc -> {

                        if (acc.getStatus() == AccountStatus.PARTIAL_KYC_PENDING) {

                            acc.setStatus(AccountStatus.ACTIVE);
                            accountRepo.save(acc);
                        }
                    });
        }
    }

}
