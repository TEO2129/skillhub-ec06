package com.example.auth.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Service HMAC - TP3
 * Calcule et vérifie les signatures HMAC-SHA256.
 */
public class HmacService {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Calcule un HMAC-SHA256 d'un message avec une clé.
     */
    public static String calculateHmac(String message, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Erreur HMAC", e);
        }
    }

    /**
     * Compare deux HMAC en temps constant (anti-attaque par timing).
     */
    public static boolean constantTimeEquals(String hmac1, String hmac2) {
        if (hmac1 == null || hmac2 == null) return false;
        return MessageDigest.isEqual(
                hmac1.getBytes(StandardCharsets.UTF_8),
                hmac2.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}