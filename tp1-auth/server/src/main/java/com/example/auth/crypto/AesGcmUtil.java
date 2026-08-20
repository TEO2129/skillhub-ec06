package com.example.auth.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String VERSION = "v1";

    // ✅ VALIDATION DE LA CLÉ (au début des méthodes)
    private static void validateMasterKey(String masterKey) {
        if (masterKey == null) {
            throw new IllegalArgumentException("La Master Key ne peut pas être null.");
        }
        int length = masterKey.getBytes(StandardCharsets.UTF_8).length;
        // AES supporte 16, 24 ou 32 bytes
        if (length != 16 && length != 24 && length != 32) {
            throw new IllegalArgumentException(
                    "La Master Key doit faire 16, 24 ou 32 bytes. Longueur actuelle : " + length + " bytes."
            );
        }
    }

    public static String encrypt(String plainText, String masterKey) {
        // ✅ Validation au début
        validateMasterKey(masterKey);

        try {
            byte[] keyBytes = masterKey.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            String ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertext);

            return VERSION + ":" + ivBase64 + ":" + ciphertextBase64;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de chiffrement AES GCM", e);
        }
    }

    public static String decrypt(String encryptedData, String masterKey) {
        // ✅ Validation au début
        validateMasterKey(masterKey);

        try {
            String[] parts = encryptedData.split(":");
            if (parts.length != 3 || !parts[0].equals(VERSION)) {
                throw new IllegalArgumentException("Format de donnees chiffrees invalide");
            }

            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

            byte[] keyBytes = masterKey.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de dechiffrement AES GCM", e);
        }
    }

    public static boolean isValidMasterKey(String masterKey) {
        if (masterKey == null) return false;
        int length = masterKey.getBytes(StandardCharsets.UTF_8).length;
        return length == 16 || length == 24 || length == 32;
    }
}