package com.example.UnityTrustBank.Service;

import com.example.UnityTrustBank.dto.*;

public interface CustomerProfileService {

    CustomerProfileResponseDto createProfile(
            Long userId,
            CustomerProfileCreateDto dto
    );

    CustomerProfileResponseDto getProfile(Long userId);

    void updateDocuments(Long userId, String aadhaar, String pan);

    void verifyAadhaar(Long userId);

    void verifyPan(Long userId);
}
