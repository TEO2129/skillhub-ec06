package com.example.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HmacService {

    public static String calculateHmac(String message, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erreur HMAC", e);
        }
    }

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
