package com.login.Login.service.otp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.login.Login.dto.otp.OtpResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final LoadingCache<String, OtpEntry> otpCache;
    private final LoadingCache<String, String> emailToRefId;
    private final LoadingCache<String, Long> blockedUsers;

    private static final int MAX_OTP_VERIFICATION_ATTEMPTS = 5;
    private static final int MAX_OTP_REGENERATIONS = 3;
    private static final long OTP_EXPIRATION_MINUTES = 3;
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

        blockedUsers = CacheBuilder.newBuilder()
                .expireAfterWrite(OTP_COOLDOWN_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public Long load(String key) {
                        return null;
                    }
                });
    }

    public OtpResponse generateOtp(String email) {
        if (blockedUsers.getIfPresent(email) != null) {
            long remaining = getRemainingBlockTime(email);
            throw new RuntimeException("Account is blocked. Try again after " + remaining + " seconds.");
        }

        String existingRefId = emailToRefId.getIfPresent(email);
        if (existingRefId != null) {
            OtpEntry existingEntry = otpCache.getIfPresent(existingRefId);

            if (existingEntry != null) {
                if (existingEntry.getAttempts().incrementAndGet() > MAX_OTP_VERIFICATION_ATTEMPTS) {
                    blockUser(email);
                    otpCache.invalidate(existingRefId);
                    emailToRefId.invalidate(email);
                    throw new RuntimeException("Too many attempts. Account blocked for 15 minutes.");
                }

                if (existingEntry.getRegenerations() >= MAX_OTP_REGENERATIONS) {
                    long cooldownRemaining = getRemainingCooldownTime(existingEntry);
                    throw new RuntimeException(
                            "Maximum OTP regenerations reached. Try again after " + cooldownRemaining + " seconds."
                    );
                }

                existingEntry.incrementRegenerations();
                existingEntry.resetOtp(generateRandomOtp(), TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES));

                return new OtpResponse(existingRefId, existingEntry.getOtp());
            }
        }

        String refId = UUID.randomUUID().toString();
        String otp = generateRandomOtp();
        OtpEntry entry = new OtpEntry(email, otp, TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES));

        otpCache.put(refId, entry);
        emailToRefId.put(email, refId);

        return new OtpResponse(refId, otp);
    }

    /** Generate OTP and return refId */
    public OtpResponse generateOtpUsingId(Long id) {
        if (blockedUsers.getIfPresent(String.valueOf(id)) != null) {
            long remaining = getRemainingBlockTime(String.valueOf(id));
            throw new RuntimeException("Account is blocked. Try again after " + remaining + " seconds.");
        }
        String existingRefId = emailToRefId.getIfPresent(String.valueOf(id));
        if (existingRefId != null) {
            OtpEntry existingEntry = otpCache.getIfPresent(existingRefId);

            if (existingEntry != null) {
                if (existingEntry.getAttempts().incrementAndGet() > MAX_OTP_VERIFICATION_ATTEMPTS) {
                    blockUser(String.valueOf(id));
                    otpCache.invalidate(existingRefId);
                    emailToRefId.invalidate(String.valueOf(id));
                    throw new RuntimeException("Too many attempts. Account blocked for 15 minutes.");
                }

                if (existingEntry.getRegenerations() >= MAX_OTP_REGENERATIONS) {
                    long cooldownRemaining = getRemainingCooldownTime(existingEntry);
                    throw new RuntimeException(
                            "Maximum OTP regenerations reached. Try again after " + cooldownRemaining + " seconds."
                    );
                }

                existingEntry.incrementRegenerations();
                existingEntry.resetOtp(generateRandomOtp(), TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES));

                return new OtpResponse(existingRefId, existingEntry.getOtp());
            }
        }

        String refId = UUID.randomUUID().toString();
        String otp = generateRandomOtp();
        OtpEntry entry = new OtpEntry(String.valueOf(id), otp, TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES));

        otpCache.put(refId, entry);
        emailToRefId.put(String.valueOf(id), refId);

        return new OtpResponse(refId, otp);
    }

    public boolean verifyOtp(String refId, String otpInput) {
        OtpEntry entry = otpCache.getIfPresent(refId);
        if (entry == null) return false;

        long remainingSeconds = getRemainingOtpTime(refId);
        if (remainingSeconds <= 0) {
            otpCache.invalidate(refId);
            emailToRefId.invalidate(entry.getEmail());
            return false; // OTP expired
        }

        // Increment attempts
        if (entry.getAttempts().incrementAndGet() > MAX_OTP_VERIFICATION_ATTEMPTS) {
            blockUser(entry.getEmail());
            otpCache.invalidate(refId);
            emailToRefId.invalidate(entry.getEmail());
            return false;
        }

        boolean valid = entry.getOtp().equals(otpInput);
        if (valid) {
            // clear on success
            otpCache.invalidate(refId);
            emailToRefId.invalidate(entry.getEmail());
        }
        return valid;
    }



    private void blockUser(String email) {
        blockedUsers.put(email, System.currentTimeMillis());
    }


    public long getRemainingBlockTime(String email) {
        Long blockedAt = blockedUsers.getIfPresent(email);
        if (blockedAt == null) return 0;

        long elapsedMillis = System.currentTimeMillis() - blockedAt;
        long totalMillis = TimeUnit.MINUTES.toMillis(OTP_COOLDOWN_MINUTES);
        long remainingMillis = totalMillis - elapsedMillis;

        return Math.max(remainingMillis / 1000, 0);
    }





public long getRemainingOtpTime(String refId) {
        OtpEntry entry = otpCache.getIfPresent(refId);
        if (entry == null) return 0;

        long elapsedMillis = System.currentTimeMillis() - entry.getCreatedAt();
        long expiryMinutes = (entry.getRegenerations() > MAX_OTP_REGENERATIONS) ? OTP_COOLDOWN_MINUTES : OTP_EXPIRATION_MINUTES;
        long remainingMillis = TimeUnit.MINUTES.toMillis(expiryMinutes) - elapsedMillis;

        return Math.max(remainingMillis / 1000, 0);
    }


    public int remainingAttempts(String refId) {
        OtpEntry entry = otpCache.getIfPresent(refId);
        if (entry == null) return 0;
        return MAX_OTP_VERIFICATION_ATTEMPTS - entry.getAttempts().get();
    }


    public OtpEntry getOtpEntry(String refId) {
        return otpCache.getIfPresent(refId);
    }


    private String generateRandomOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }


    private long getRemainingCooldownTime(OtpEntry entry) {
        long elapsedMillis = System.currentTimeMillis() - entry.getCreatedAt();
        long remainingMillis = TimeUnit.MINUTES.toMillis(OTP_COOLDOWN_MINUTES) - elapsedMillis;
        return Math.max(remainingMillis / 1000, 0);
    }
}
