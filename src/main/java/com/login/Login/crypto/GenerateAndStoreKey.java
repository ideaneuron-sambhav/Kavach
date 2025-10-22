package com.login.Login.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

public class GenerateAndStoreKey {

    public static void main(String[] args) throws Exception {
        // --- Generate AES Key ---
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey key = kg.generateKey();
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        String keyId = "v1";

        // --- Generate fixed IV (12 bytes for GCM) ---
        byte[] iv = new byte[16]; // 16 bytes = 128 bits
        new SecureRandom().nextBytes(iv);
        String base64Iv = Base64.getEncoder().encodeToString(iv);

        // --- Print results ---
        System.out.println("===== AES Key & Fixed IV Generated Successfully =====");
        System.out.println("Key ID: " + keyId);
        System.out.println("Base64 Key: " + base64Key);
        System.out.println("AES_FIXED_IV (Base64): " + base64Iv);

        System.out.println("\n--- Add these to your environment or application.properties ---");
        System.out.println("APP_KEYS_v1=" + base64Key);
        System.out.println("AES_FIXED_IV=" + base64Iv);
    }
}
