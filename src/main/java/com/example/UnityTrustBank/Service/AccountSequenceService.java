package com.example.UnityTrustBank.Service;

public interface AccountSequenceService {

    String generateAccountNumber(Long branchId); // REAL USE (approve time)

    String previewNextAccountNumber(Long branchId); // PREVIEW ONLY
}
