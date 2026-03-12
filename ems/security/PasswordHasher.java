package ems.security;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {
    private PasswordHasher() {}

    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null) return false;
        return BCrypt.checkpw(rawPassword, passwordHash);
    }
}