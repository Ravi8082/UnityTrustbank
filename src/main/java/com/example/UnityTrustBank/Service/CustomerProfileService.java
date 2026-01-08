package com.example.UnityTrustBank.Service;

import com.example.UnityTrustBank.Entity.CustomerProfile;
import com.example.UnityTrustBank.dto.CustomerProfileDto;

public interface CustomerProfileService {

    CustomerProfile createProfile(Long userId, CustomerProfileDto dto);

    CustomerProfile updateDocuments(Long userId, String aadhaar, String pan);

    void verifyAadhaar(Long userId, Long managerId);

    void verifyPan(Long userId, Long managerId);

    CustomerProfile getProfileByUser(Long userId);
}
