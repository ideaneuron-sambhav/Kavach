package com.login.Login.crypto;

import java.util.Base64;

public class EnvKeyProvider implements KeyProvider {

    private final String activeKeyId;
    private final byte[] keyBytes;
    private final byte[] ivBytes;

    public EnvKeyProvider() {
        // Read the active key ID (e.g., "v1")
        this.activeKeyId = System.getenv().getOrDefault("APP_ACTIVE_KEY_ID", "v1");

        // Fetch AES key from environment
        String base64Key = System.getenv("APP_KEYS_" + activeKeyId);
        if (base64Key == null) {
            throw new IllegalStateException("Environment variable APP_KEYS_" + activeKeyId + " not set");
        }
        this.keyBytes = Base64.getDecoder().decode(base64Key);

        // Validate AES key length: 16 / 24 / 32 bytes
        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("Invalid AES key length");
        }

        // Fetch IV from environment
        String base64Iv = System.getenv("AES_FIXED_IV_" + activeKeyId);
        if (base64Iv == null) {
            throw new IllegalStateException("Environment variable AES_FIXED_IV_" + activeKeyId + " not set");
        }
        this.ivBytes = Base64.getDecoder().decode(base64Iv);

        // Validate IV length: must be 16 bytes for AES
        if (ivBytes.length != 16) {
            throw new IllegalArgumentException("Invalid IV length (must be 16 bytes)");
        }
    }

    @Override
    public byte[] getKeyBytes(String keyId) {
        return keyBytes;
    }

    @Override
    public byte[] getIvBytes(String keyId) {
        return ivBytes;
    }

    @Override
    public String getActiveKeyId() {
        return activeKeyId;
    }
}
