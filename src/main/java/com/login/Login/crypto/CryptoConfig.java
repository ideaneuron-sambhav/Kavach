package com.login.Login.crypto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfig {

    @Bean
    public KeyProvider keyProvider() {
        return new EnvKeyProvider();
    }

    @Bean
    public AesGcmCryptoService aesGcmCryptoService(KeyProvider keyProvider) {
        return new AesGcmCryptoService(keyProvider);
    }
}
