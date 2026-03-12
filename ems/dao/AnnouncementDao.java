package ems.dao;

import ems.model.Announcement;
import ems.model.AnnouncementTarget;

import java.sql.*;
import java.util.*;

public class AnnouncementDao {
    public long insert(Announcement a) throws SQLException {
        String sql = "INSERT INTO announcements (title, body, posted_at, author_id, author_name, target) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)";
        try (Connection c = ems.db.Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getBody());
            ps.setLong(3, a.getAuthorId());
            ps.setString(4, a.getAuthorName());
            ps.setString(5, a.getTarget().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key");
                return keys.getLong(1);
            }
        }
    }

    public List<Announcement> listForUser(ems.model.User user) throws SQLException {
        String sql = "SELECT * FROM announcements WHERE " +
                "target = ? OR target = 'ALL' ORDER BY posted_at DESC";
        try (Connection c = ems.db.Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getRole().name());
            try (ResultSet rs = ps.executeQuery()) {
                List<Announcement> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    private Announcement map(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setId(rs.getLong("id"));
        a.setTitle(rs.getString("title"));
        a.setBody(rs.getString("body"));
        a.setPostedAt(rs.getTimestamp("posted_at"));
        a.setAuthorId(rs.getLong("author_id"));
        a.setAuthorName(rs.getString("author_name"));
        a.setTarget(AnnouncementTarget.valueOf(rs.getString("target")));
        return a;
    }
}