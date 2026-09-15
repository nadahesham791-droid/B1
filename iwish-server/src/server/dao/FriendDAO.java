package server.dao;

import common.dto.FriendRequestDTO;
import common.dto.UserDTO;
import server.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {

    public List<UserDTO> getFriends(int userId) throws SQLException {
        List<UserDTO> friends = new ArrayList<>();
        // Either sender_id = userId and status = ACCEPTED (then friend is receiver_id),
        // or receiver_id = userId and status = ACCEPTED (then friend is sender_id)
        String sql = "SELECT u.user_id, u.username, u.email, u.full_name, u.balance " +
                     "FROM users u " +
                     "JOIN friend_requests fr ON ( " +
                     "   (fr.sender_id = ? AND fr.receiver_id = u.user_id) OR " +
                     "   (fr.receiver_id = ? AND fr.sender_id = u.user_id) " +
                     ") " +
                     "WHERE fr.status = 'ACCEPTED' " +
                     "GROUP BY u.user_id ORDER BY u.full_name ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friends.add(new UserDTO(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("full_name"),
                            rs.getDouble("balance")
                    ));
                }
            }
        }
        return friends;
    }

    public List<FriendRequestDTO> getPendingRequests(int userId) throws SQLException {
        List<FriendRequestDTO> requests = new ArrayList<>();
        String sql = "SELECT fr.request_id, fr.sender_id, fr.receiver_id, fr.status, fr.created_at, " +
                     "s.username AS sender_user, s.full_name AS sender_name, " +
                     "r.username AS receiver_user, r.full_name AS receiver_name " +
                     "FROM friend_requests fr " +
                     "JOIN users s ON fr.sender_id = s.user_id " +
                     "JOIN users r ON fr.receiver_id = r.user_id " +
                     "WHERE fr.receiver_id = ? AND fr.status = 'PENDING' " +
                     "ORDER BY fr.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FriendRequestDTO dto = new FriendRequestDTO();
                    dto.setRequestId(rs.getInt("request_id"));
                    dto.setSenderId(rs.getInt("sender_id"));
                    dto.setReceiverId(rs.getInt("receiver_id"));
                    dto.setStatus(rs.getString("status"));
                    dto.setSenderUsername(rs.getString("sender_user"));
                    dto.setSenderFullName(rs.getString("sender_name"));
                    dto.setReceiverUsername(rs.getString("receiver_user"));
                    dto.setReceiverFullName(rs.getString("receiver_name"));
                    dto.setCreatedAt(rs.getString("created_at"));
                    requests.add(dto);
                }
            }
        }
        return requests;
    }

    public boolean sendFriendRequest(int senderId, int receiverId) throws SQLException {
        if (senderId == receiverId) return false;

        // Check if any request or friendship already exists
        String checkSql = "SELECT request_id, status FROM friend_requests WHERE " +
                          "(sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, senderId);
            checkPs.setInt(2, receiverId);
            checkPs.setInt(3, receiverId);
            checkPs.setInt(4, senderId);

            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("ACCEPTED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) {
                        return false; // Already connected or pending
                    }
                    // If declined, update to pending
                    int reqId = rs.getInt("request_id");
                    String updateSql = "UPDATE friend_requests SET sender_id = ?, receiver_id = ?, status = 'PENDING' WHERE request_id = ?";
                    try (PreparedStatement upPs = conn.prepareStatement(updateSql)) {
                        upPs.setInt(1, senderId);
                        upPs.setInt(2, receiverId);
                        upPs.setInt(3, reqId);
                        return upPs.executeUpdate() > 0;
                    }
                }
            }
        }

        String insertSql = "INSERT INTO friend_requests (sender_id, receiver_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean acceptFriendRequest(int requestId, int receiverId) throws SQLException {
        String sql = "UPDATE friend_requests SET status = 'ACCEPTED' WHERE request_id = ? AND receiver_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, receiverId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean declineFriendRequest(int requestId, int receiverId) throws SQLException {
        String sql = "UPDATE friend_requests SET status = 'DECLINED' WHERE request_id = ? AND receiver_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, receiverId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean removeFriend(int userId, int friendId) throws SQLException {
        String sql = "DELETE FROM friend_requests WHERE " +
                     "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                     "AND status = 'ACCEPTED'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.setInt(3, friendId);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean areFriends(int user1, int user2) throws SQLException {
        String sql = "SELECT request_id FROM friend_requests WHERE " +
                     "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                     "AND status = 'ACCEPTED'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user1);
            ps.setInt(2, user2);
            ps.setInt(3, user2);
            ps.setInt(4, user1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
