package ems.service;

import ems.dao.UserDao;
import ems.model.AccountStatus;
import ems.model.Role;
import ems.model.User;
import ems.security.PasswordHasher;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public void resetPassword(long userId, String newPassword) throws SQLException {
        setPassword(userId, newPassword);
    }

    public void setPassword(long userId, String newRawPassword) throws SQLException {
        if (newRawPassword == null || newRawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        userDao.updatePasswordHash(userId, PasswordHasher.hash(newRawPassword));
    }

    public User login(String email, String rawPassword) throws SQLException {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (rawPassword == null || rawPassword.isBlank()) throw new IllegalArgumentException("Password is required");

        email = email.trim().toLowerCase();

        User u = userDao.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Invalid email or password"));

        if (!PasswordHasher.verify(rawPassword, u.getPasswordHash())) {
            throw new IllegalStateException("Invalid email or password");
        }

        if (u.getStatus() == AccountStatus.PENDING) {
            throw new IllegalStateException("Account pending manager approval");
        }
        if (u.getStatus() == AccountStatus.REJECTED) {
            throw new IllegalStateException("Account was rejected by manager");
        }

        userDao.updateLastLogin(u.getId());
        return u;
    }

    public User register(String email,
                         String rawPassword,
                         Role role,
                         String fullName,
                         String gamerTag,
                         String gender,
                         Date dateOfBirth,
                         String address) throws SQLException {

        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (rawPassword == null || rawPassword.isBlank()) throw new IllegalArgumentException("Password is required");
        if (role == null) throw new IllegalArgumentException("Role is required");

        email = email.trim().toLowerCase();

        // ✅ Server-side enforcement: only PLAYER + VIEWER can self-register
        if (role == Role.ADMIN || role == Role.MANAGER) {
            throw new IllegalStateException("Only Players and Viewers can register. Managers are created by Admin.");
        }

        Optional<User> existing = userDao.findByEmail(email);
        if (existing.isPresent()) throw new IllegalStateException("Email already registered");

        User u = new User();
        u.setEmail(email);
        u.setFullName(emptyToNull(fullName));
        u.setGamerTag(emptyToNull(gamerTag));
        u.setGender(emptyToNull(gender));
        u.setDateOfBirth(dateOfBirth);
        u.setAddress(emptyToNull(address));

        u.setPasswordHash(PasswordHasher.hash(rawPassword));
        u.setRole(role);

        // ✅ Role-based default status
        if (role == Role.PLAYER) {
            u.setStatus(AccountStatus.PENDING);
        } else { // VIEWER
            u.setStatus(AccountStatus.APPROVED);
        }

        long id = userDao.insertUser(u);
        return userDao.findById(id).orElseThrow(() -> new SQLException("Failed to load newly created user"));
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}