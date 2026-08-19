package com.example.auth;

import com.example.auth.crypto.AesGcmUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTS DE LA MASTER KEY - TP4
 *
 * Teste le chiffrement et déchiffrement AES GCM avec Master Key.
 * La clé de test doit faire exactement 32 caractères (256 bits).
 */
@ActiveProfiles("test")
public class AesGcmUtilTest {

    // ✅ 32 caractères exactement (256 bits)
    private static final String TEST_KEY = "12345678901234567890123456789012";

    @Test
    void testEncryptDecrypt_OK() {
        String plainText = "Password123!";
        String encrypted = AesGcmUtil.encrypt(plainText, TEST_KEY);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = AesGcmUtil.decrypt(encrypted, TEST_KEY);
        assertEquals(plainText, decrypted);
    }

    @Test
    void testDecrypt_WithWrongKey_ShouldThrow() {
        String plainText = "Password123!";
        String encrypted = AesGcmUtil.encrypt(plainText, TEST_KEY);

        assertThrows(RuntimeException.class,
                () -> AesGcmUtil.decrypt(encrypted, "wrong_key"));
    }

    @Test
    void testEncrypt_GeneratesDifferentResultsEachTime() {
        String plainText = "Password123!";
        String encrypted1 = AesGcmUtil.encrypt(plainText, TEST_KEY);
        String encrypted2 = AesGcmUtil.encrypt(plainText, TEST_KEY);
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testIsValidMasterKey_OK() {
        assertTrue(AesGcmUtil.isValidMasterKey("12345678901234567890123456789012"));
        assertFalse(AesGcmUtil.isValidMasterKey("short"));
        assertFalse(AesGcmUtil.isValidMasterKey(null));
    }
}