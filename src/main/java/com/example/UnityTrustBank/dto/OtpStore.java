package com.example.UnityTrustBank.dto;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OtpStore {

    private static final Map<String, OtpData> store = new ConcurrentHashMap<>();

    private static final Set<String> verifiedEmails =
            ConcurrentHashMap.newKeySet();

    static class OtpData {
        int otp;
        long expiryTime;
        int attempts;
    }

    public static void save(String email, int otp) {
        OtpData data = new OtpData();
        data.otp = otp;
        data.expiryTime = System.currentTimeMillis() + (2 * 60 * 1000); // 2 min
        data.attempts = 0;
        store.put(email, data);
    }

    public static boolean verify(String email, int otp) {
        OtpData data = store.get(email);

        if (data == null) return false;

        if (System.currentTimeMillis() > data.expiryTime) {
            store.remove(email);
            return false;
        }

        data.attempts++;

        if (data.attempts > 5) {
            store.remove(email);
            throw new RuntimeException("Too many attempts. Resend OTP.");
        }

        if (data.otp == otp) {
            store.remove(email);
            verifiedEmails.add(email);   // ✅ MARK VERIFIED
            return true;
        }

        return false;
    }

    public static boolean isVerified(String email) {
        return verifiedEmails.contains(email);
    }

    public static void clear(String email) {
        store.remove(email);
        verifiedEmails.remove(email);
    }
}
