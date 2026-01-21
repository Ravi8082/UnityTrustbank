package com.example.UnityTrustBank.Service;

import java.math.BigDecimal;

public interface UpiService {

    // Create a new UPI ID for the account
    void createUpi(Long accountId, String vpa, String pin);

    // Perform a UPI payment from one VPA to another
    void pay(
        String fromVpa,
        String toVpa,
        String pin,
        BigDecimal amount,
        String remark
    );
}
