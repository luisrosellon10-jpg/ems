package ems.otp;

import java.security.SecureRandom;

public final class OtpGenerator {
    private static final SecureRandom RNG = new SecureRandom();

    private OtpGenerator() {}

    public static String generateDigits(int length) {
        if (length <= 0) throw new IllegalArgumentException("length must be > 0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RNG.nextInt(10));
        }
        return sb.toString();
    }
}