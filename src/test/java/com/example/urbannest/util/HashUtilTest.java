package com.example.urbannest.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashUtilTest {

    @Test
    void generateHash_returnsConsistentHash() {
        String hash1 = HashUtil.generateHash("test-input");
        String hash2 = HashUtil.generateHash("test-input");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void generateHash_returnsSha512Length() {
        String hash = HashUtil.generateHash("test");
        assertThat(hash).hasSize(128); // SHA-512 = 512 bits = 128 hex chars
    }

    @Test
    void generateHash_differentInputsProduceDifferentHashes() {
        String hash1 = HashUtil.generateHash("input1");
        String hash2 = HashUtil.generateHash("input2");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void generateHash_returnsLowercaseHex() {
        String hash = HashUtil.generateHash("test");
        assertThat(hash).matches("[0-9a-f]+");
    }
}
