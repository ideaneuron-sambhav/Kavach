package com.login.Login.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generic AES encryption converter for any string field.
 * Automatically encrypts when saving to DB and decrypts when reading.
 */
@Component
@Converter
public class AesAttributeConverter implements AttributeConverter<String, String> {

    private static AesGcmCryptoService staticAesService;

    @Autowired
    public void setAesService(AesGcmCryptoService aesService) {
        // static setter so JPA (which creates converter) can use Spring-managed service
        AesAttributeConverter.staticAesService = aesService;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) return null;
            return staticAesService.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting field: " + e.getMessage(), e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) return null;
            return staticAesService.decrypt(dbData);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting field: " + e.getMessage(), e);
        }
    }
}
