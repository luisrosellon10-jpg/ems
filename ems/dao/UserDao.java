package ems.dao;

import ems.db.Db;
import ems.model.AccountStatus;
import ems.model.Role;
import ems.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    
    public void updateProfile(long userId,
                          String fullName,
                          String gamerTag,
                          String gender,
                          java.sql.Date dateOfBirth,
                          String address) throws SQLException {

    String sql = "UPDATE users SET full_name=?, gamer_tag=?, gender=?, date_of_birth=?, address=?, updated_at=NOW() WHERE id=?";

    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, fullName);
        ps.setString(2, gamerTag);
        ps.setString(3, gender);

        if (dateOfBirth == null) ps.setNull(4, Types.DATE);
        else ps.setDate(4, dateOfBirth);

        ps.setString(5, address);
        ps.setLong(6, userId);

        ps.executeUpdate();
    }
}
    
    public List<User> listApprovedPlayers() throws SQLException {
    String sql =
            "SELECT id, email, full_name, gamer_tag, gender, date_of_birth, address, password_hash, role, status, created_at, updated_at, last_login " +
            "FROM users " +
            "WHERE role = 'PLAYER' AND status = 'APPROVED' " +
            "ORDER BY gamer_tag IS NULL, gamer_tag ASC, email ASC";

    List<User> out = new ArrayList<>();
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) out.add(map(rs));
    }
    return out;
}

    private static final String BASE_SELECT =
            "SELECT id, email, full_name, gamer_tag, gender, date_of_birth, address, " +
            "password_hash, role, status, created_at, updated_at, last_login " +
            "FROM users ";

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = BASE_SELECT + "WHERE email = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<User> findById(long id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    // Backwards-compatible overload (AdminBootstrapTool/SeedAdmin can call this)
    public long insertUser(String email, String passwordHash, Role role, AccountStatus status) throws SQLException {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        u.setStatus(status);
        return insertUser(u);
    }

    // New insert that includes profile columns
    public long insertUser(User u) throws SQLException {
        String sql = "INSERT INTO users (" +
                "email, full_name, gamer_tag, gender, date_of_birth, address, " +
                "password_hash, role, status" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getEmail());
            ps.setString(2, u.getFullName());
            ps.setString(3, u.getGamerTag());
            ps.setString(4, u.getGender());
            ps.setDate(5, u.getDateOfBirth());   // java.sql.Date (nullable)
            ps.setString(6, u.getAddress());

            ps.setString(7, u.getPasswordHash());
            ps.setString(8, u.getRole().name());
            ps.setString(9, u.getStatus().name());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key returned for user insert");
                return keys.getLong(1);
            }
        }
    }

    public void updatePasswordHash(long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateLastLogin(long userId) throws SQLException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    public List<User> listPendingUsers() throws SQLException {
    String sql = BASE_SELECT + "WHERE status = 'PENDING' AND role = 'PLAYER' ORDER BY created_at ASC";
    List<User> out = new ArrayList<>();
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) out.add(map(rs));
    }
    return out;
}

    public void updateStatus(long userId, AccountStatus status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));

        // Profile fields
        u.setFullName(rs.getString("full_name"));
        u.setGamerTag(rs.getString("gamer_tag"));
        u.setGender(rs.getString("gender"));
        u.setDateOfBirth(rs.getDate("date_of_birth"));
        u.setAddress(rs.getString("address"));

        // Auth/system fields
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setStatus(AccountStatus.valueOf(rs.getString("status")));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        u.setLastLogin(rs.getTimestamp("last_login"));
        return u;
    }
}