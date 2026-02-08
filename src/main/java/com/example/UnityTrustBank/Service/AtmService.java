package com.example.UnityTrustBank.Service;

import java.math.BigDecimal;
import java.util.List;

import com.example.UnityTrustBank.Entity.AtmCard;
import com.example.UnityTrustBank.Entity.AtmReceipt;
import com.example.UnityTrustBank.Entity.AtmRequest;
import com.example.UnityTrustBank.Enum.Request;
import com.example.UnityTrustBank.dto.AtmCardDetailsDto;

public interface AtmService {

    // ATM Request
    void requestAtm(String accountNumber);

    void approveAtm(Long requestId);

    void rejectAtm(Long requestId, String reason);

    // Withdraw (USE CARD NUMBER ❗)
    AtmReceipt withdraw(String cardNumber, String pin, BigDecimal amount);

    // Reset PIN (USE ACCOUNT ID)
    void resetPin(Long accountId, String oldPin, String newPin);

    // OTP
    String sendPinResetOtp(String cardNumber);

    void resetPinWithOtp(String cardNumber, int otp, String newPin);

    // Requests
    List<AtmRequest> pendingAtmRequests();

    boolean hasPendingAtmRequest(Long userId);

    List<AtmRequest> findByRequestedBy_IdAndStatus(Long userId, Request status);

    // Card
    AtmCard getUserAtmCard(Long accountId);

    AtmCardDetailsDto getCardDetails(Long accountId);
    List<AtmReceipt> getReceipts(Long accountId);

}
