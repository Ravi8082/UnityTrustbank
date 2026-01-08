package com.example.UnityTrustBank.ServiceImple;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.UnityTrustBank.Entity.CustomerProfile;
import com.example.UnityTrustBank.Entity.User;
import com.example.UnityTrustBank.Enum.AppRole;
import com.example.UnityTrustBank.Repository.CustomerProfileRepo;
import com.example.UnityTrustBank.Repository.UserRepo;
import com.example.UnityTrustBank.Service.CustomerProfileService;
import com.example.UnityTrustBank.dto.CustomerProfileDto;

@Service
public class CustomerProfileServiceImpl implements CustomerProfileService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CustomerProfileRepo customerProfileRepo;

    // ================= CREATE PROFILE =================
    @Override
    public CustomerProfile createProfile(Long userId, CustomerProfileDto dto) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getCustomerProfile() != null) {
            throw new RuntimeException("Customer profile already exists");
        }

        if (dto.getDob() == null) {
            throw new RuntimeException("DOB is mandatory");
        }

        CustomerProfile profile = new CustomerProfile();
        profile.setFullName(dto.getFullName());
        profile.setFatherName(dto.getFatherName());
        profile.setAddress(dto.getAddress());
        profile.setDob(dto.getDob());

        int age = Period.between(dto.getDob(), LocalDate.now()).getYears();
        if (age < 18) {
            profile.setBranchVisitRequired(true);
        }

        profile.setAadhaarVerified(false);
        profile.setPanVerified(false);
        profile.setUser(user);

        return customerProfileRepo.save(profile);
    }

    // ================= UPDATE DOCUMENTS =================
    @Override
    public CustomerProfile updateDocuments(Long userId, String aadhaar, String pan) {

        CustomerProfile profile = getProfileByUser(userId);

        if (profile.isAadhaarVerified() || profile.isPanVerified()) {
            throw new RuntimeException("Verified documents cannot be changed");
        }

        profile.setAadhaar(aadhaar);
        profile.setPan(pan);

        return customerProfileRepo.save(profile);
    }

    // ================= VERIFY AADHAAR =================
    @Override
    public void verifyAadhaar(Long userId, Long managerId) {

        validateManager(managerId);

        CustomerProfile profile = getProfileByUser(userId);

        if (profile.getAadhaar() == null) {
            throw new RuntimeException("Aadhaar not submitted");
        }

        profile.setAadhaarVerified(true);
        customerProfileRepo.save(profile);
    }

    // ================= VERIFY PAN =================
    @Override
    public void verifyPan(Long userId, Long managerId) {

        validateManager(managerId);

        CustomerProfile profile = getProfileByUser(userId);

        if (profile.getPan() == null) {
            throw new RuntimeException("PAN not submitted");
        }

        profile.setPanVerified(true);
        customerProfileRepo.save(profile);
    }

    // ================= GET PROFILE =================
    @Override
    public CustomerProfile getProfileByUser(Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = user.getCustomerProfile();
        if (profile == null) {
            throw new RuntimeException("Customer profile not found");
        }
        return profile;
    }

    // ================= MANAGER VALIDATION =================
    private User validateManager(Long managerId) {

        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() == null ||
            manager.getRole().getRoleName() != AppRole.ROLE_ADMIN) {
            throw new RuntimeException("Only manager can verify documents");
        }
        return manager;
    }
}
