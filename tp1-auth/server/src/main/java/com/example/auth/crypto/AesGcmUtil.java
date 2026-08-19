package com.example.auth.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * TP4 - Chiffrement AES GCM avec Master Key.
 * Format de stockage : "v1:Base64(iv):Base64(ciphertext)"
 */
public class AesGcmUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12; // bytes (96 bits)
    private static final String VERSION = "v1";

    /**
     * Chiffre un texte en clair avec une clé Master Key.
     *
     * @param plainText le texte à chiffrer
     * @param masterKey la clé maître (256 bits)
     * @return format "v1:Base64(iv):Base64(ciphertext)"
     */
    public static String encrypt(String plainText, String masterKey) {
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

    /**
     * Déchiffre un texte chiffré avec une clé Master Key.
     *
     * @param encryptedData format "v1:Base64(iv):Base64(ciphertext)"
     * @param masterKey la clé maître (256 bits)
     * @return le texte en clair
     */
    public static String decrypt(String encryptedData, String masterKey) {
        try {
            // Parser le format
            String[] parts = encryptedData.split(":");
            if (parts.length != 3 || !parts[0].equals(VERSION)) {
                throw new IllegalArgumentException("Format de données chiffrées invalide");
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
            throw new RuntimeException("Erreur de déchiffrement AES GCM - Données corrompues ou mauvaise clé", e);
        }
    }

    /**
     * Vérifie si la Master Key est valide (256 bits).
     */
    public static boolean isValidMasterKey(String masterKey) {
        if (masterKey == null || masterKey.isEmpty()) return false;
        return masterKey.getBytes(StandardCharsets.UTF_8).length >= 32; // 256 bits
    }
}