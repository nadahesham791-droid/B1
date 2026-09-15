package server.dao;

import common.dto.ContributionDTO;
import common.dto.ProductDTO;
import common.dto.WishListItemDTO;
import server.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishListDAO {

    public List<WishListItemDTO> getWishListByUser(int userId) throws SQLException {
        List<WishListItemDTO> list = new ArrayList<>();
        String sql = "SELECT w.item_id, w.user_id, w.product_id, w.status, " +
                     "u.full_name AS owner_name, " +
                     "p.name AS product_name, p.description, p.price, p.category, p.image_url, " +
                     "COALESCE(SUM(c.amount), 0.0) AS paid_amount " +
                     "FROM wish_list_items w " +
                     "JOIN users u ON w.user_id = u.user_id " +
                     "JOIN products p ON w.product_id = p.product_id " +
                     "LEFT JOIN contributions c ON w.item_id = c.item_id " +
                     "WHERE w.user_id = ? " +
                     "GROUP BY w.item_id, w.user_id, w.product_id, w.status, u.full_name, " +
                     "p.name, p.description, p.price, p.category, p.image_url " +
                     "ORDER BY w.item_id DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WishListItemDTO item = mapWishListItem(rs);
                    item.setContributions(getContributionsForItem(conn, item.getItemId()));
                    list.add(item);
                }
            }
        }
        return list;
    }

    public WishListItemDTO getWishListItemById(int itemId) throws SQLException {
        String sql = "SELECT w.item_id, w.user_id, w.product_id, w.status, " +
                     "u.full_name AS owner_name, " +
                     "p.name AS product_name, p.description, p.price, p.category, p.image_url, " +
                     "COALESCE(SUM(c.amount), 0.0) AS paid_amount " +
                     "FROM wish_list_items w " +
                     "JOIN users u ON w.user_id = u.user_id " +
                     "JOIN products p ON w.product_id = p.product_id " +
                     "LEFT JOIN contributions c ON w.item_id = c.item_id " +
                     "WHERE w.item_id = ? " +
                     "GROUP BY w.item_id, w.user_id, w.product_id, w.status, u.full_name, " +
                     "p.name, p.description, p.price, p.category, p.image_url";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WishListItemDTO item = mapWishListItem(rs);
                    item.setContributions(getContributionsForItem(conn, item.getItemId()));
                    return item;
                }
            }
        }
        return null;
    }

    public boolean addToWishList(int userId, int productId) throws SQLException {
        // Check if item already exists in user's active wishlist
        String checkSql = "SELECT item_id FROM wish_list_items WHERE user_id = ? AND product_id = ? AND status = 'AVAILABLE'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, userId);
            checkPs.setInt(2, productId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) return false; // Already in wishlist
            }
        }

        String sql = "INSERT INTO wish_list_items (user_id, product_id, status) VALUES (?, ?, 'AVAILABLE')";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean removeFromWishList(int userId, int itemId) throws SQLException {
        String sql = "DELETE FROM wish_list_items WHERE item_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markCompleted(Connection conn, int itemId) throws SQLException {
        String sql = "UPDATE wish_list_items SET status = 'COMPLETED' WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<ContributionDTO> getContributionsForItem(Connection conn, int itemId) throws SQLException {
        List<ContributionDTO> list = new ArrayList<>();
        String sql = "SELECT c.contribution_id, c.item_id, c.contributor_id, c.amount, c.contributed_at, " +
                     "u.full_name AS contributor_name " +
                     "FROM contributions c " +
                     "JOIN users u ON c.contributor_id = u.user_id " +
                     "WHERE c.item_id = ? ORDER BY c.contributed_at ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ContributionDTO(
                            rs.getInt("contribution_id"),
                            rs.getInt("item_id"),
                            rs.getInt("contributor_id"),
                            rs.getString("contributor_name"),
                            rs.getDouble("amount"),
                            rs.getString("contributed_at")
                    ));
                }
            }
        }
        return list;
    }

    private WishListItemDTO mapWishListItem(ResultSet rs) throws SQLException {
        ProductDTO prod = new ProductDTO(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getString("category"),
                rs.getString("image_url")
        );

        double paid = rs.getDouble("paid_amount");
        double remaining = Math.max(0.0, prod.getPrice() - paid);

        WishListItemDTO item = new WishListItemDTO();
        item.setItemId(rs.getInt("item_id"));
        item.setUserId(rs.getInt("user_id"));
        item.setOwnerName(rs.getString("owner_name"));
        item.setProduct(prod);
        item.setStatus(rs.getString("status"));
        item.setPaidAmount(paid);
        item.setRemainingAmount(remaining);
        return item;
    }
}
