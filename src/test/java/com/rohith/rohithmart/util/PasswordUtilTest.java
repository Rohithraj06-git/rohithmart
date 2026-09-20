package com.rohith.rohithmart.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    @DisplayName("Password hashing produces valid BCrypt hash and verifies successfully")
    void testHashAndVerifyPassword() {
        String rawPassword = "SecurePassword@123";
        String hash = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertTrue(PasswordUtil.verifyPassword(rawPassword, hash));
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hash));
    }

    @Test
    @DisplayName("Empty password throws IllegalArgumentException")
    void testEmptyPasswordThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
    }

    @Test
    @DisplayName("Verify seed hash matches Password@123")
    void testSeedHashVerification() {
        String seedHash = "$2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG";
        assertTrue(PasswordUtil.verifyPassword("Password@123", seedHash));
    }
}
