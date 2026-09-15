package server.dao;

import common.dto.NotificationDTO;
import server.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public NotificationDTO createNotification(int userId, String type, String message) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, type, message, is_read) VALUES (?, ?, ?, FALSE)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, message);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        return new NotificationDTO(id, userId, type, message, false, "Just now");
                    }
                }
            }
        }
        return null;
    }

    public List<NotificationDTO> getUserNotifications(int userId) throws SQLException {
        List<NotificationDTO> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, type, message, is_read, created_at FROM notifications " +
                     "WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new NotificationDTO(
                            rs.getInt("notification_id"),
                            rs.getInt("user_id"),
                            rs.getString("type"),
                            rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public boolean markAllRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;
        }
    }
}
