package ems.dao;

import ems.db.Db;
import ems.model.Tournament;
import ems.model.TournamentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentDao {
    
    public java.util.Optional<ems.model.Tournament> findById(long id) throws java.sql.SQLException {
    String sql = "SELECT id, name, start_date, end_date, status, created_at, updated_at FROM tournaments WHERE id = ?";
    try (java.sql.Connection c = ems.db.Db.getConnection();
         java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, id);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return java.util.Optional.empty();

            ems.model.Tournament t = new ems.model.Tournament();
            t.setId(rs.getLong("id"));
            t.setName(rs.getString("name"));
            t.setStartDate(rs.getDate("start_date"));
            t.setEndDate(rs.getDate("end_date"));
            t.setStatus(ems.model.TournamentStatus.valueOf(rs.getString("status")));
            t.setCreatedAt(rs.getTimestamp("created_at"));
            t.setUpdatedAt(rs.getTimestamp("updated_at"));
            return java.util.Optional.of(t);
        }
    }
}
    
    public void updateStatus(long tournamentId, ems.model.TournamentStatus status) throws java.sql.SQLException {
    String sql = "UPDATE tournaments SET status = ?, updated_at = NOW() WHERE id = ?";
    try (java.sql.Connection c = ems.db.Db.getConnection();
         java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, status.name());
        ps.setLong(2, tournamentId);
        ps.executeUpdate();
    }
}

    private static final String BASE_SELECT =
            "SELECT id, name, start_date, end_date, status, created_at, updated_at FROM tournaments ";

    public List<Tournament> listAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY created_at DESC";
        List<Tournament> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    public long insert(Tournament t) throws SQLException {
        String sql = "INSERT INTO tournaments (name, start_date, end_date, status) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setDate(2, t.getStartDate());
            ps.setDate(3, t.getEndDate());
            ps.setString(4, t.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key for tournament insert");
                return keys.getLong(1);
            }
        }
    }

    public void update(Tournament t) throws SQLException {
        String sql = "UPDATE tournaments SET name = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setDate(2, t.getStartDate());
            ps.setDate(3, t.getEndDate());
            ps.setString(4, t.getStatus().name());
            ps.setLong(5, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM tournaments WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Tournament map(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setId(rs.getLong("id"));
        t.setName(rs.getString("name"));
        t.setStartDate(rs.getDate("start_date"));
        t.setEndDate(rs.getDate("end_date"));
        t.setStatus(TournamentStatus.valueOf(rs.getString("status")));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));
        return t;
    }
}