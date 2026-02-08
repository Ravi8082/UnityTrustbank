package com.example.UnityTrustBank.Service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.UnityTrustBank.dto.AccountApplicationCreateDto;
import com.example.UnityTrustBank.dto.AccountApplicationResponseDto;

public interface AccountApplicationService {

    AccountApplicationResponseDto apply(
            AccountApplicationCreateDto dto
    );

    void uploadKycImages(
            Long applicationId,
            MultipartFile profileImage,
            MultipartFile aadhaarImage,
            MultipartFile panImage
    );

    List<AccountApplicationResponseDto> pendingForManager();

    List<AccountApplicationResponseDto> pendingForBranch(Long branchId);

    void approve(Long applicationId);

    void reject(Long applicationId, String reason);

    AccountApplicationResponseDto getApplicationById(Long id);

    // NEW - missing in your interface
    List<AccountApplicationResponseDto> getAllApplicationsForManager();

    List<AccountApplicationResponseDto> getAllApplicationsForBranch(Long branchId);
}
