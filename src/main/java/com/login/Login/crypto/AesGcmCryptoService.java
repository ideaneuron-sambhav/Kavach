package com.login.Login.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesGcmCryptoService {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits

    private final KeyProvider keyProvider;

    public AesGcmCryptoService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String encrypt(String plaintext) throws Exception {
        if (plaintext == null) return null;

        // Get active key and IV from KeyProvider
        String keyId = keyProvider.getActiveKeyId();
        byte[] keyBytes = keyProvider.getKeyBytes(keyId);
        byte[] ivBytes = keyProvider.getIvBytes(keyId);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);

        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Combine IV + ciphertext (optional, if you want dynamic IV handling)
        byte[] combined = new byte[ivBytes.length + ciphertext.length];
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(ciphertext, 0, combined, ivBytes.length, ciphertext.length);

        return keyId + "|" + Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String encrypted) throws Exception {
        if (encrypted == null) return null;

        String[] parts = encrypted.split("\\|", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Invalid encrypted format");

        String keyId = parts[0];
        byte[] combined = Base64.getDecoder().decode(parts[1]);

        byte[] ivBytes = keyProvider.getIvBytes(keyId);

        if (combined.length < ivBytes.length + 1) {
            throw new IllegalArgumentException("Invalid encrypted data");
        }

        // Extract ciphertext (skip IV bytes if you stored IV separately)
        byte[] ciphertext = new byte[combined.length - ivBytes.length];
        System.arraycopy(combined, ivBytes.length, ciphertext, 0, ciphertext.length);

        byte[] keyBytes = keyProvider.getKeyBytes(keyId);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);

        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, "UTF-8");
    }
}
