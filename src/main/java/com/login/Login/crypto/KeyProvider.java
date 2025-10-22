package com.login.Login.crypto;

public interface KeyProvider {
    byte[] getKeyBytes(String keyId);
    byte[] getIvBytes(String keyId);
    String getActiveKeyId();
}
