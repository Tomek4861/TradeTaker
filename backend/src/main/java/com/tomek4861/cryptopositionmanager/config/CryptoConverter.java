package com.tomek4861.cryptopositionmanager.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Converter
@Component
public class CryptoConverter implements AttributeConverter<String, String> {

    private final static String ALGORITHM = "AES/ECB/PKCS5Padding";
    private final Key key;

    public CryptoConverter(@Value("${app.encryption.secret-key}") String secret) {

        this.key = new SecretKeySpec(secret.getBytes(), "AES");
    }


    @Override
    public String convertToDatabaseColumn(String attribute) {
        // before save to db
        if (attribute == null) return null;

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));

        } catch (Exception e) {
            throw new IllegalStateException("Could not encrypt attribute", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // after retrieving from db
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));


        } catch (Exception e) {
            throw new IllegalStateException("Could not decrypt attribute", e);
        }
    }

}


