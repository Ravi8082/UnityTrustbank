package com.example.UnityTrustBank.Service;

import java.math.BigDecimal;
import java.util.List;

import com.example.UnityTrustBank.Entity.AtmRequest;
public interface AtmService {

    void requestAtm(String accountId);

    void approveAtm(Long requestId);

    void rejectAtm(Long requestId, String reason);

    void withdraw(Long cardId, String pin, BigDecimal amount);

    void resetPin(Long cardId, String oldPin, String newPin);

    List<AtmRequest> pendingAtmRequests();


}
