package com.login.Login.crypto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Converter
public class EncryptedJsonConverter implements AttributeConverter<Map<String,Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AesAttributeConverter aesConverter;

    @Autowired
    public EncryptedJsonConverter(AesAttributeConverter aesConverter) {
        this.aesConverter = aesConverter;
    }

    @Override
    public String convertToDatabaseColumn(Map<String,Object> attribute) {
        if(attribute == null) return null;
        try {
            String json = objectMapper.writeValueAsString(attribute);
            return aesConverter.convertToDatabaseColumn(json); // encrypt
        } catch(Exception e) {
            throw new RuntimeException("Error encrypting JSON", e);
        }
    }

    @Override
    public Map<String,Object> convertToEntityAttribute(String dbData) {
        if(dbData == null) return null;
        try {
            String json = aesConverter.convertToEntityAttribute(dbData); // decrypt
            return objectMapper.readValue(json, Map.class);
        } catch(Exception e) {
            throw new RuntimeException("Error decrypting JSON", e);
        }
    }
}
