package ems.dao;

import ems.db.Db;
import ems.model.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamDao {
    
    public List<Team> listByTournamentExcluding(long tournamentId, long excludeTeamId) throws SQLException {
    String sql = BASE_SELECT + "WHERE tournament_id = ? AND id <> ? ORDER BY name ASC";
    List<Team> out = new ArrayList<>();
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, tournamentId);
        ps.setLong(2, excludeTeamId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
    }
    return out;
}
    
    public List<Team> listEligibleForMatches(long tournamentId, int minPlayers) throws SQLException {
        String sql =
                "SELECT t.id, t.tournament_id, t.name, t.created_at, t.updated_at " +
                "FROM teams t " +
                "JOIN (SELECT team_id, COUNT(*) cnt FROM players GROUP BY team_id) p ON p.team_id = t.id " +
                "WHERE t.tournament_id = ? AND p.cnt >= ? " +
                "ORDER BY t.name ASC";

        List<Team> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setInt(2, minPlayers);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs)); // reuse your existing map(rs)
            }
        }
        return out;
    }

    private static final String BASE_SELECT =
            "SELECT id, tournament_id, name, created_at, updated_at FROM teams ";

    public List<Team> listByTournament(long tournamentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE tournament_id = ? ORDER BY name ASC";
        List<Team> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public Optional<Team> findById(long id) throws SQLException {
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

    public Optional<Team> findByTournamentAndName(long tournamentId, String name) throws SQLException {
        String sql = BASE_SELECT + "WHERE tournament_id = ? AND name = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public long insert(long tournamentId, String name) throws SQLException {
        String sql = "INSERT INTO teams (tournament_id, name) VALUES (?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, tournamentId);
            ps.setString(2, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key for team insert");
                return keys.getLong(1);
            }
        }
    }

    public void update(long teamId, String name) throws SQLException {
        String sql = "UPDATE teams SET name = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, teamId);
            ps.executeUpdate();
        }
    }

    public void delete(long teamId) throws SQLException {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teamId);
            ps.executeUpdate();
        }
    }

    private Team map(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setId(rs.getLong("id"));
        t.setTournamentId(rs.getLong("tournament_id"));
        t.setName(rs.getString("name"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));
        return t;
    }
}