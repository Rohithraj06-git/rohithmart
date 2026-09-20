package com.rohith.rohithmart.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords securely using BCrypt.
 * Plaintext storage and MD5/SHA1 are strictly prohibited.
 */
public final class PasswordUtil {

    private static final int LOG_ROUNDS = 10;

    private PasswordUtil() {
    }

    /**
     * Hashes a plaintext password using BCrypt with salt rounds.
     *
     * @param plainTextPassword the plaintext password
     * @return 60-character bcrypt hash
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param plainTextPassword the candidate password
     * @param hashedPassword the stored hash
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
