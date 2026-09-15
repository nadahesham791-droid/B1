package server.dao;

import common.dto.UserDTO;
import server.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public UserDTO login(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, email, full_name, balance FROM users WHERE (username = ? OR email = ?) AND password_hash = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserDTO(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("full_name"),
                            rs.getDouble("balance")
                    );
                }
            }
        }
        return null;
    }

    public UserDTO register(String username, String email, String password, String fullName) throws SQLException {
        String checkSql = "SELECT user_id FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, username);
            checkPs.setString(2, email);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return null; // Already exists
                }
            }
        }

        String insertSql = "INSERT INTO users (username, email, password_hash, full_name, balance) VALUES (?, ?, ?, ?, 1000.00)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, fullName);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        return new UserDTO(id, username, email, fullName, 1000.00);
                    }
                }
            }
        }
        return null;
    }

    public UserDTO getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, email, full_name, balance FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserDTO(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("full_name"),
                            rs.getDouble("balance")
                    );
                }
            }
        }
        return null;
    }

    public List<UserDTO> searchUsers(String query, int excludeUserId) throws SQLException {
        List<UserDTO> list = new ArrayList<>();
        String sql = "SELECT user_id, username, email, full_name, balance FROM users " +
                     "WHERE user_id != ? AND (LOWER(username) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(email) LIKE ?) " +
                     "ORDER BY full_name ASC LIMIT 20";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query.toLowerCase() + "%";
            ps.setInt(1, excludeUserId);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserDTO(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("full_name"),
                            rs.getDouble("balance")
                    ));
                }
            }
        }
        return list;
    }

    public boolean updateBalance(Connection conn, int userId, double amountDelta) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE user_id = ? AND (balance + ?) >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amountDelta);
            ps.setInt(2, userId);
            ps.setDouble(3, amountDelta);
            return ps.executeUpdate() > 0;
        }
    }
}
