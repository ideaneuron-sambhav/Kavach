package com.login.Login.service.otp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.login.Login.dto.otp.OtpResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final LoadingCache<String, OtpEntry> otpCache;
    private final LoadingCache<String, String> emailToRefId; // track refId per email

    private static final int MAX_OTP_VERIFICATION_ATTEMPTS = 5;
    private static final int MAX_OTP_REGENERATIONS = 3;
    private static final long OTP_EXPIRATION_MINUTES = 1;
    private static final long OTP_COOLDOWN_MINUTES = 15; // cooldown after max regenerations
    private static final SecureRandom random = new SecureRandom();

    public OtpService() {
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(OTP_COOLDOWN_MINUTES, TimeUnit.MINUTES) // max expiry = cooldown
                .build(new CacheLoader<>() {
                    @Override
                    public OtpEntry load(String key) {
                        return null; // only use getIfPresent
                    }
                });

        emailToRefId = CacheBuilder.newBuilder()
                .expireAfterWrite(OTP_COOLDOWN_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return null;
                    }
                });
    }

    /** Generate OTP and return refId */
    public OtpResponse generateOtp(String email) {
        String existingRefId = emailToRefId.getIfPresent(email);
        if (existingRefId != null) {
            OtpEntry existingEntry = otpCache.getIfPresent(existingRefId);

            if (existingEntry != null) {
                // Check if max regenerations reached
                if (existingEntry.getRegenerations() >= MAX_OTP_REGENERATIONS) {
                    long cooldownRemaining = getRemainingCooldownTime(existingEntry);
                    throw new RuntimeException(
                            "Maximum OTP regenerations reached. Try after " + cooldownRemaining + " seconds"
                    );
                }

                // Increment regenerations and reset OTP + time
                existingEntry.incrementRegenerations();
                existingEntry.resetOtp(generateRandomOtp(), TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES));

                long remainingTime = getRemainingOtpTime(existingRefId);
                return new OtpResponse(existingRefId, existingEntry.getOtp());
            }
        }

        // Create new OTP
        String refId = java.util.UUID.randomUUID().toString();
        String otp = generateRandomOtp();
        OtpEntry entry = new OtpEntry(email, otp, TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES));
        otpCache.put(refId, entry);
        emailToRefId.put(email, refId);
        long remainingTime = getRemainingOtpTime(refId);
        return new OtpResponse(refId, otp);
    }

    /** Verify OTP using refId */
    public boolean verifyOtp(String refId, String otpInput) {
        OtpEntry entry = otpCache.getIfPresent(refId);
        if (entry == null) return false;
        long remainingSeconds = getRemainingOtpTime(refId);
        if (remainingSeconds <= 0) {
            otpCache.invalidate(refId);
            emailToRefId.invalidate(entry.getEmail());
            return false; // OTP expired
        }

        if (entry.getAttempts().incrementAndGet() > MAX_OTP_VERIFICATION_ATTEMPTS) {
            otpCache.invalidate(refId);
            emailToRefId.invalidate(entry.getEmail());
            return false;
        }

        boolean valid = entry.getOtp().equals(otpInput);
        if (valid) {
            otpCache.invalidate(refId);
            emailToRefId.invalidate(entry.getEmail());
        }
        return valid;
    }

    /** Remaining OTP time in seconds */
    public long getRemainingOtpTime(String refId) {
        OtpEntry entry = otpCache.getIfPresent(refId);
        if (entry == null) return 0;

        long elapsedMillis = System.currentTimeMillis() - entry.getCreatedAt();
        long expiryMinutes = (entry.getRegenerations() > MAX_OTP_REGENERATIONS) ? OTP_COOLDOWN_MINUTES : OTP_EXPIRATION_MINUTES;
        long remainingMillis = TimeUnit.MINUTES.toMillis(expiryMinutes) - elapsedMillis;

        return Math.max(remainingMillis / 1000, 0);
    }

    /** Remaining attempts for OTP verification */
    public int remainingAttempts(String refId) {
        OtpEntry entry = otpCache.getIfPresent(refId);
        if (entry == null) return 0;
        return MAX_OTP_VERIFICATION_ATTEMPTS - entry.getAttempts().get();
    }

    /** Get OTP entry by refId */
    public OtpEntry getOtpEntry(String refId) {
        return otpCache.getIfPresent(refId);
    }

    /** Helper: generate 6-digit OTP */
    private String generateRandomOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    /** Helper: remaining cooldown time in seconds */
    private long getRemainingCooldownTime(OtpEntry entry) {
        long elapsedMillis = System.currentTimeMillis() - entry.getCreatedAt();
        long remainingMillis = TimeUnit.MINUTES.toMillis(OTP_COOLDOWN_MINUTES) - elapsedMillis;
        return Math.max(remainingMillis / 1000, 0);
    }
}
