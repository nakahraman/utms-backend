package com.utms.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String ALG = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;  // standart
    private static final int IV_BYTES = 12;       // GCM için önerilen

    private final SecretKey key;

    public CryptoService(@Value("${app.crypto.aesKeyBase64}") String aesKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(aesKeyBase64);
            if (keyBytes.length != 32) {
                throw new IllegalStateException("AES key must be exactly 32 bytes (Base64-decoded) for AES-256.");
            }
            this.key = new SecretKeySpec(keyBytes, "AES");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid Base64 in app.crypto.aesKeyBase64. Remove quotes and ensure valid Base64.", e);
        }
    }

    public EncryptedPayload encrypt(byte[] plain) {
        try {
            byte[] iv = new byte[IV_BYTES];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] cipherBytes = cipher.doFinal(plain);
            return new EncryptedPayload(cipherBytes, iv, ALG);

        } catch (Exception ex) {
            throw new RuntimeException("Encryption failed", ex);
        }
    }

    public byte[] decrypt(byte[] cipherBytes, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(cipherBytes);

        } catch (Exception ex) {
            throw new RuntimeException("Decryption failed", ex);
        }
    }

    public record EncryptedPayload(byte[] cipherBytes, byte[] iv, String alg) {}
}
