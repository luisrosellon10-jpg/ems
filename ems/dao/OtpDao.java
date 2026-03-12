package ems.dao;

import ems.db.Db;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class OtpDao {

    public void createOtp(long userId, String otpHash, Instant expiresAt) throws SQLException {
        // Mark older OTPs as used to avoid confusion
        String invalidate = "UPDATE otp_codes SET used = TRUE WHERE user_id = ? AND used = FALSE";
        String insert = "INSERT INTO otp_codes (user_id, otp_hash, expires_at, attempts, used) VALUES (?, ?, ?, 0, FALSE)";

        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(invalidate);
                 PreparedStatement ps2 = c.prepareStatement(insert)) {
                ps1.setLong(1, userId);
                ps1.executeUpdate();

                ps2.setLong(1, userId);
                ps2.setString(2, otpHash);
                ps2.setTimestamp(3, Timestamp.from(expiresAt));
                ps2.executeUpdate();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public Optional<OtpRow> findLatestActiveOtp(long userId) throws SQLException {
        String sql = "SELECT id, otp_hash, expires_at, attempts, used " +
                     "FROM otp_codes WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                OtpRow row = new OtpRow(
                        rs.getLong("id"),
                        rs.getString("otp_hash"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getInt("attempts"),
                        rs.getBoolean("used")
                );
                return Optional.of(row);
            }
        }
    }

    public void incrementAttempts(long otpId) throws SQLException {
        String sql = "UPDATE otp_codes SET attempts = attempts + 1 WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, otpId);
            ps.executeUpdate();
        }
    }

    public void markUsed(long otpId) throws SQLException {
        String sql = "UPDATE otp_codes SET used = TRUE WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, otpId);
            ps.executeUpdate();
        }
    }

    public record OtpRow(long id, String otpHash, Instant expiresAt, int attempts, boolean used) {}
}