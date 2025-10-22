package com.login.Login.security;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtBlacklistService {

    private final Map<String, BlacklistEntry> blacklist = new ConcurrentHashMap<>();
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    // Class to store expiry and reason
    @Data
    public static class BlacklistEntry {
        private final long expiryMillis;
        private final String reason;

        public BlacklistEntry(long expiryMillis, String reason) {
            this.expiryMillis = expiryMillis;
            this.reason = reason;
        }

        public long getExpiryMillis() {
            return expiryMillis;
        }

        public String getReason() {
            return reason;
        }
    }

    // Blacklist a token with a reason
    public void blacklistToken(String token, long expiryMillis, String reason) {
        blacklist.put(token, new BlacklistEntry(expiryMillis, reason));
        activeTokens.values().removeIf(t -> t.equals(token));
    }

    // Check if token is blacklisted
    public boolean isBlacklisted(String token) {
        BlacklistEntry entry = blacklist.get(token);
        if (entry == null) return false;

        // Remove if expired
        if (entry.getExpiryMillis() < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    // Get reason for blacklisted token
    public String getBlacklistReason(String token) {
        BlacklistEntry entry = blacklist.get(token);
        return entry != null ? entry.getReason() : null;
    }

    // Set the active token for a user
    public void setActiveToken(String email, String token) {
        activeTokens.put(email, token);
    }

    // Get active token of a user
    public String getActiveToken(String email) {
        return activeTokens.get(email);
    }


    // Blacklist a token due to user logout
    public void logoutToken(String token, long expiryMillis) {
        blacklist.put(token, new BlacklistEntry(expiryMillis, "User logged out"));
        activeTokens.values().removeIf(t -> t.equals(token));
    }
}
