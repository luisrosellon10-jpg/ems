package ems.dao;

import ems.db.Db;
import ems.model.Match;
import ems.model.MatchStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDao {
    
    public boolean allMatchesCompletedByTournamentAndStage(long tournamentId, String stage) throws SQLException {
    String sql = "SELECT COUNT(*) total, SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) done " +
                 "FROM matches WHERE tournament_id = ? AND stage = ?";
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, tournamentId);
        ps.setString(2, stage);
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            int total = rs.getInt("total");
            int done = rs.getInt("done");
            return total > 0 && total == done;
        }
    }
}

public boolean anyMatchDrawByTournamentAndStage(long tournamentId, String stage) throws SQLException {
    String sql = "SELECT 1 FROM matches " +
                 "WHERE tournament_id = ? AND stage = ? AND score_a IS NOT NULL AND score_b IS NOT NULL AND score_a = score_b LIMIT 1";
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, tournamentId);
        ps.setString(2, stage);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}
    
    public List<Match> listByTeam(long teamId) throws SQLException {
    String sql = BASE_SELECT +
            "WHERE (team_a_id = ? OR team_b_id = ?) " +
            "ORDER BY scheduled_at IS NULL, scheduled_at ASC, id ASC";
    List<Match> out = new ArrayList<>();
    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setLong(1, teamId);
        ps.setLong(2, teamId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
    }
    return out;
}

    private static final String BASE_SELECT =
            "SELECT id, tournament_id, team_a_id, team_b_id, team_a, team_b, scheduled_at, score_a, score_b, status, stage " +
            "FROM matches ";

    // stageFilter: null or "ALL" => no stage filtering
    public List<Match> listByTournamentFiltered(long tournamentId, String stageFilter) throws SQLException {
        String stage = (stageFilter == null) ? "ALL" : stageFilter.trim().toUpperCase();

        String sql;
        if ("ALL".equals(stage)) {
            sql = BASE_SELECT + "WHERE tournament_id = ? ORDER BY scheduled_at IS NULL, scheduled_at ASC, id ASC";
        } else if ("(NULL)".equals(stage)) {
            sql = BASE_SELECT + "WHERE tournament_id = ? AND stage IS NULL ORDER BY scheduled_at IS NULL, scheduled_at ASC, id ASC";
        } else {
            sql = BASE_SELECT + "WHERE tournament_id = ? AND stage = ? ORDER BY scheduled_at IS NULL, scheduled_at ASC, id ASC";
        }

        List<Match> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            if (!"ALL".equals(stage) && !"(NULL)".equals(stage)) {
                ps.setString(2, stage);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public boolean existsByTournamentAndStage(long tournamentId, String stage) throws SQLException {
        String sql = "SELECT 1 FROM matches WHERE tournament_id = ? AND stage = ? LIMIT 1";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setString(2, stage);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Match> listByTournamentAndStage(long tournamentId, String stage) throws SQLException {
        String sql = BASE_SELECT + "WHERE tournament_id = ? AND stage = ? ORDER BY scheduled_at IS NULL, scheduled_at ASC, id ASC";
        List<Match> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setString(2, stage);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public void updateTeamIds(long matchId, Long teamAId, Long teamBId) throws SQLException {
        String sql = "UPDATE matches SET team_a_id = ?, team_b_id = ? WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (teamAId == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, teamAId);
            if (teamBId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, teamBId);

            ps.setLong(3, matchId);
            ps.executeUpdate();
        }
    }

    public List<Match> listByTournament(long tournamentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE tournament_id = ? ORDER BY scheduled_at IS NULL, scheduled_at ASC, id ASC";
        List<Match> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public Optional<Match> findById(long id) throws SQLException {
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

    public long insert(Match m) throws SQLException {
        String sql =
                "INSERT INTO matches (tournament_id, team_a_id, team_b_id, team_a, team_b, scheduled_at, score_a, score_b, status, stage) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, m.getTournamentId());

            if (m.getTeamAId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, m.getTeamAId());
            if (m.getTeamBId() == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, m.getTeamBId());

            ps.setString(4, m.getTeamA());
            ps.setString(5, m.getTeamB());

            ps.setTimestamp(6, m.getScheduledAt());

            if (m.getScoreA() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, m.getScoreA());
            if (m.getScoreB() == null) ps.setNull(8, Types.INTEGER); else ps.setInt(8, m.getScoreB());

            ps.setString(9, m.getStatus().name());
            ps.setString(10, m.getStage());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key for match insert");
                return keys.getLong(1);
            }
        }
    }

    public void update(Match m) throws SQLException {
        String sql =
                "UPDATE matches SET team_a_id=?, team_b_id=?, team_a=?, team_b=?, scheduled_at=?, score_a=?, score_b=?, status=?, stage=? WHERE id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (m.getTeamAId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, m.getTeamAId());
            if (m.getTeamBId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, m.getTeamBId());

            ps.setString(3, m.getTeamA());
            ps.setString(4, m.getTeamB());

            ps.setTimestamp(5, m.getScheduledAt());

            if (m.getScoreA() == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, m.getScoreA());
            if (m.getScoreB() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, m.getScoreB());

            ps.setString(8, m.getStatus().name());
            ps.setString(9, m.getStage());

            ps.setLong(10, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM matches WHERE id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Match map(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setId(rs.getLong("id"));
        m.setTournamentId(rs.getLong("tournament_id"));

        long aId = rs.getLong("team_a_id");
        m.setTeamAId(rs.wasNull() ? null : aId);

        long bId = rs.getLong("team_b_id");
        m.setTeamBId(rs.wasNull() ? null : bId);

        m.setTeamA(rs.getString("team_a"));
        m.setTeamB(rs.getString("team_b"));

        m.setScheduledAt(rs.getTimestamp("scheduled_at"));

        int a = rs.getInt("score_a");
        m.setScoreA(rs.wasNull() ? null : a);

        int b = rs.getInt("score_b");
        m.setScoreB(rs.wasNull() ? null : b);

        m.setStatus(MatchStatus.valueOf(rs.getString("status")));
        m.setStage(rs.getString("stage"));

        return m;
    }
}