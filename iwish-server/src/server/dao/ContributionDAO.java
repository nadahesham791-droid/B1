package server.dao;

import common.dto.UserDTO;
import common.dto.WishListItemDTO;
import server.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContributionDAO {

    public static class ContributionResult {
        public boolean success;
        public String message;
        public boolean isCompleted;
        public WishListItemDTO updatedItem;
        public int receiverUserId;
        public List<Integer> contributorUserIds = new ArrayList<>();
    }

    public ContributionResult processContribution(int itemId, int contributorId, double amount) {
        ContributionResult result = new ContributionResult();

        if (amount <= 0) {
            result.success = false;
            result.message = "Contribution amount must be greater than 0.";
            return result;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Transaction begins

            // 1. Check Contributor Balance
            String userSql = "SELECT balance, full_name FROM users WHERE user_id = ? FOR UPDATE";
            double contributorBalance;
            String contributorName;
            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setInt(1, contributorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        result.success = false;
                        result.message = "Contributor user not found.";
                        return result;
                    }
                    contributorBalance = rs.getDouble("balance");
                    contributorName = rs.getString("full_name");
                }
            }

            if (contributorBalance < amount) {
                conn.rollback();
                result.success = false;
                result.message = "Insufficient funds in your wallet. Your balance is $" + String.format("%.2f", contributorBalance);
                return result;
            }

            // 2. Lock and Check Wishlist Item
            String itemSql = "SELECT w.item_id, w.user_id, w.product_id, w.status, p.name, p.price " +
                             "FROM wish_list_items w " +
                             "JOIN products p ON w.product_id = p.product_id " +
                             "WHERE w.item_id = ? FOR UPDATE";
            int ownerId;
            String productName;
            double productPrice;
            String currentStatus;
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        result.success = false;
                        result.message = "Wishlist item not found.";
                        return result;
                    }
                    ownerId = rs.getInt("user_id");
                    productName = rs.getString("name");
                    productPrice = rs.getDouble("price");
                    currentStatus = rs.getString("status");
                }
            }

            if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                result.success = false;
                result.message = "This gift is already fully funded and completed!";
                return result;
            }

            // 3. Compute Current Contributions
            String sumSql = "SELECT COALESCE(SUM(amount), 0.0) FROM contributions WHERE item_id = ?";
            double currentPaid = 0.0;
            try (PreparedStatement ps = conn.prepareStatement(sumSql)) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) currentPaid = rs.getDouble(1);
                }
            }

            double remaining = productPrice - currentPaid;
            if (amount > (remaining + 0.001)) {
                conn.rollback();
                result.success = false;
                result.message = "Amount exceeds remaining needed ($" + String.format("%.2f", remaining) + ").";
                return result;
            }

            // 4. Deduct money from contributor
            String deductSql = "UPDATE users SET balance = balance - ? WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deductSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, contributorId);
                ps.executeUpdate();
            }

            // 5. Insert contribution
            String insertContSql = "INSERT INTO contributions (item_id, contributor_id, amount) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertContSql)) {
                ps.setInt(1, itemId);
                ps.setInt(2, contributorId);
                ps.setDouble(3, amount);
                ps.executeUpdate();
            }

            double newTotalPaid = currentPaid + amount;
            boolean isCompleted = (newTotalPaid >= productPrice - 0.001);

            // 6. If completed, update status
            if (isCompleted) {
                String completeSql = "UPDATE wish_list_items SET status = 'COMPLETED' WHERE item_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(completeSql)) {
                    ps.setInt(1, itemId);
                    ps.executeUpdate();
                }
            }

            conn.commit(); // Transaction ends successfully

            // Fetch list of distinct contributors for notification
            Set<Integer> distinctContributors = new HashSet<>();
            String distinctSql = "SELECT DISTINCT contributor_id FROM contributions WHERE item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(distinctSql)) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        distinctContributors.add(rs.getInt("contributor_id"));
                    }
                }
            }

            result.success = true;
            result.isCompleted = isCompleted;
            result.receiverUserId = ownerId;
            result.contributorUserIds.addAll(distinctContributors);
            result.message = isCompleted ?
                    "🎉 Congratulations! Your contribution completed the gift for " + productName + "!" :
                    "Thank you! You contributed $" + String.format("%.2f", amount) + ". Remaining: $" + String.format("%.2f", Math.max(0, productPrice - newTotalPaid));

            return result;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            result.success = false;
            result.message = "Transaction error: " + e.getMessage();
            return result;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
