package com.example.auth;

import com.example.auth.crypto.AesGcmUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AesGcmUtilTest {

    private static final String TEST_KEY = "test_master_key_256bits_long_enough12345678";

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
        assertTrue(AesGcmUtil.isValidMasterKey("12345678901234567890123456789012")); // 32 chars
        assertFalse(AesGcmUtil.isValidMasterKey("short"));
        assertFalse(AesGcmUtil.isValidMasterKey(null));
    }
}