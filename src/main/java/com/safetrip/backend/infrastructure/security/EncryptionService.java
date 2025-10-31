package com.safetrip.backend.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey key;

    public EncryptionService(@Value("${encryption.key}") String keyBase64) {
        byte[] decodedKey = Base64.getDecoder().decode(keyBase64);
        this.key = new SecretKeySpec(decodedKey, ALGORITHM);
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando valor", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - IV_LENGTH];

            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new RuntimeException("Error desencriptando valor", e);
        }
    }
}