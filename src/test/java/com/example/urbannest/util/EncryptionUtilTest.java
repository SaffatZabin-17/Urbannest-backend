package com.example.urbannest.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionUtilTest {

    // Valid AES-256 key: 32 bytes of 'A' (0x41), base64-encoded
    private static final String TEST_KEY = "QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=";

    @Test
    void encryptThenDecrypt_returnsOriginalText() {
        String plainText = "1234567890";
        String encrypted = EncryptionUtil.encrypt(plainText, TEST_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, TEST_KEY);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void encrypt_producesUniqueCiphertextEachTime() {
        String plainText = "test-nid";
        String encrypted1 = EncryptionUtil.encrypt(plainText, TEST_KEY);
        String encrypted2 = EncryptionUtil.encrypt(plainText, TEST_KEY);
        assertThat(encrypted1).isNotEqualTo(encrypted2); // random IV per encryption
    }

    @Test
    void encrypt_outputIsBase64Encoded() {
        String encrypted = EncryptionUtil.encrypt("test", TEST_KEY);
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        assertThat(decoded.length).isGreaterThan(12); // at least IV (12) + ciphertext
    }

    @Test
    void decrypt_withWrongKey_throws() {
        String encrypted = EncryptionUtil.encrypt("test", TEST_KEY);
        // Different key: 32 bytes of 'B' (0x42)
        String wrongKey = "QkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkI=";

        assertThatThrownBy(() -> EncryptionUtil.decrypt(encrypted, wrongKey))
                .isInstanceOf(RuntimeException.class);
    }
}
