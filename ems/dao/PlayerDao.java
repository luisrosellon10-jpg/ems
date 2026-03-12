package ems.dao;

import ems.db.Db;
import ems.model.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDao {
    
    public boolean isAvailableByEmail(String email) throws SQLException {
    // Available means:
    // - no player record exists yet, OR
    // - player record exists but team_id is NULL or 0 (depends on your DB)
    String sql = "SELECT team_id FROM players WHERE contact_email = ? LIMIT 1";
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, email);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return true; // no record yet => available

            long teamId = rs.getLong(1);
            boolean wasNull = rs.wasNull();

            if (wasNull) return true;   // team_id NULL => available
            return teamId <= 0;         // team_id 0 => available
        }
    }
}
    
    public java.util.Optional<Player> findByContactEmail(String email) throws java.sql.SQLException {
    String sql = BASE_SELECT + "WHERE contact_email = ?";
    try (java.sql.Connection c = ems.db.Db.getConnection();
         java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, email);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return java.util.Optional.empty();
            return java.util.Optional.of(map(rs));
        }
    }
}

public void assignApprovedUserToTeam(ems.model.User u, long teamId) throws java.sql.SQLException {
    // If player already exists => transfer to team
    java.util.Optional<Player> existing = findByContactEmail(u.getEmail());
    if (existing.isPresent()) {
    Player p = existing.get();
    if (p.getTeamId() == teamId) {
        // already in team => no action
        return;
    }
    transfer(p.getId(), teamId);
    return;
}

    // else insert new player record
    Player p = new Player();
    p.setTeamId(teamId);
    p.setGamerTag(u.getGamerTag());
    p.setFullName(u.getFullName());
    p.setGender(u.getGender());
    p.setDateOfBirth(u.getDateOfBirth());
    p.setContactEmail(u.getEmail());
    p.setPhone(null);

    insert(p);
}
    
    public void transfer(long playerId, long newTeamId) throws SQLException {
    String sql = "UPDATE players SET team_id = ? WHERE id = ?";
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, newTeamId);
        ps.setLong(2, playerId);
        ps.executeUpdate();
    }
}
    
    public int countByTeam(long teamId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM players WHERE team_id = ?";
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, teamId);
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}

    private static final String BASE_SELECT =
            "SELECT id, team_id, gamer_tag, full_name, gender, date_of_birth, contact_email, phone, created_at, updated_at " +
            "FROM players ";

    public List<Player> listByTeam(long teamId) throws SQLException {
        String sql = BASE_SELECT + "WHERE team_id = ? ORDER BY gamer_tag ASC";
        List<Player> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public Optional<Player> findById(long id) throws SQLException {
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

    public long insert(Player p) throws SQLException {
        String sql = "INSERT INTO players (team_id, gamer_tag, full_name, gender, date_of_birth, contact_email, phone) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, p.getTeamId());
            ps.setString(2, p.getGamerTag());
            ps.setString(3, p.getFullName());
            ps.setString(4, p.getGender());
            ps.setDate(5, p.getDateOfBirth());
            ps.setString(6, p.getContactEmail());
            ps.setString(7, p.getPhone());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key for player insert");
                return keys.getLong(1);
            }
        }
    }

    public void update(Player p) throws SQLException {
        String sql = "UPDATE players SET gamer_tag=?, full_name=?, gender=?, date_of_birth=?, contact_email=?, phone=? WHERE id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getGamerTag());
            ps.setString(2, p.getFullName());
            ps.setString(3, p.getGender());
            ps.setDate(4, p.getDateOfBirth());
            ps.setString(5, p.getContactEmail());
            ps.setString(6, p.getPhone());
            ps.setLong(7, p.getId());

            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM players WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Player map(ResultSet rs) throws SQLException {
        Player p = new Player();
        p.setId(rs.getLong("id"));
        p.setTeamId(rs.getLong("team_id"));
        p.setGamerTag(rs.getString("gamer_tag"));
        p.setFullName(rs.getString("full_name"));
        p.setGender(rs.getString("gender"));
        p.setDateOfBirth(rs.getDate("date_of_birth"));
        p.setContactEmail(rs.getString("contact_email"));
        p.setPhone(rs.getString("phone"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}