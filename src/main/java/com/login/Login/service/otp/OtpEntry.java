package com.login.Login.service.otp;

import java.util.concurrent.atomic.AtomicInteger;

public class OtpEntry {

    private final String email;
    private String otp;
    private final AtomicInteger attempts = new AtomicInteger(0);
    private final AtomicInteger regenerations = new AtomicInteger(0);
    private long createdAt;
    private long expiryTime;

    public OtpEntry(String email, String otp, long otpValidityMillis) {
        this.email = email;
        setOtp(otp, otpValidityMillis);
    }

    public String getEmail() { return email; }
    public String getOtp() { return otp; }
    public AtomicInteger getAttempts() { return attempts; }
    public int getRegenerations() { return regenerations.get(); }
    public int incrementRegenerations() { return regenerations.incrementAndGet(); }
    public long getCreatedAt() { return createdAt; }
    public long getExpiryTime() { return expiryTime; }

    /** Reset OTP and refresh creation + expiry */
    public void resetOtp(String newOtp, long otpValidityMillis) {
        setOtp(newOtp, otpValidityMillis);
        this.attempts.set(0); // reset attempts
    }

    private void setOtp(String otp, long otpValidityMillis) {
        this.otp = otp;
        this.createdAt = System.currentTimeMillis();
        this.expiryTime = createdAt + otpValidityMillis;
    }

    /** Check if OTP is expired */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
