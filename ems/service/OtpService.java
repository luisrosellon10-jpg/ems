package ems.service;

import ems.config.AppConfig;
import ems.dao.OtpDao;
import ems.dao.UserDao;
import ems.model.User;
import ems.otp.OtpGenerator;
import ems.security.PasswordHasher;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class OtpService {

    private final OtpDao otpDao = new OtpDao();
    private final UserDao userDao = new UserDao();
    private final EmailService emailService = new EmailService();

    public void sendOtpToEmail(String email) throws SQLException {
    if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
    email = email.trim().toLowerCase();

    int len = AppConfig.getInt("otp.length");
    int expiryMin = AppConfig.getInt("otp.expiry.minutes");

    // DEV MODE toggle: allow sending OTP even if user doesn't exist
    boolean allowUnknown = "true".equalsIgnoreCase(AppConfig.get("otp.allowSendToUnknownEmail"));

    User user = userDao.findByEmail(email).orElse(null);
    if (user == null && !allowUnknown) {
        throw new IllegalStateException("No account with that email");
    }

    String otp = OtpGenerator.generateDigits(len);
    String otpHash = PasswordHasher.hash(otp);
    Instant expiresAt = Instant.now().plus(expiryMin, ChronoUnit.MINUTES);

    if (user != null) {
        // Normal flow: save OTP in DB for verification/reset
        otpDao.createOtp(user.getId(), otpHash, expiresAt);
    } else {
        // DEV ONLY: we can send, but we cannot verify/reset because we have no userId
        System.out.println("[EMS OTP DEV] No user for " + email + " -> sending OTP anyway (not stored in DB).");
    }

    emailService.sendOtpEmail(email, otp, expiryMin);
}

    public long verifyOtp(String email, String otpInput) throws SQLException {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (otpInput == null || otpInput.isBlank()) throw new IllegalArgumentException("OTP is required");

        email = email.trim().toLowerCase();
        User user = userDao.findByEmail(email).orElseThrow(() -> new IllegalStateException("No account with that email"));

        var rowOpt = otpDao.findLatestActiveOtp(user.getId());
        if (rowOpt.isEmpty()) throw new IllegalStateException("No OTP requested");

        var row = rowOpt.get();
        int maxAttempts = AppConfig.getInt("otp.maxAttempts");

        if (row.used()) throw new IllegalStateException("OTP already used. Request a new OTP.");
        if (Instant.now().isAfter(row.expiresAt())) throw new IllegalStateException("OTP expired. Request a new OTP.");
        if (row.attempts() >= maxAttempts) throw new IllegalStateException("Too many OTP attempts. Request a new OTP.");

        boolean ok = PasswordHasher.verify(otpInput.trim(), row.otpHash());
        if (!ok) {
            otpDao.incrementAttempts(row.id());
            throw new IllegalStateException("Incorrect OTP");
        }

        otpDao.markUsed(row.id());
        return user.getId();
    }
}