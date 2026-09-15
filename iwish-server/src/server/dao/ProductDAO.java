package server.dao;

import common.dto.ProductDTO;
import server.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<ProductDTO> getAllProducts() throws SQLException {
        List<ProductDTO> products = new ArrayList<>();
        String sql = "SELECT product_id, name, description, price, category, image_url FROM products ORDER BY name ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(new ProductDTO(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("image_url")
                ));
            }
        }
        return products;
    }

    public ProductDTO addProduct(String name, String description, double price, String category, String imageUrl) throws SQLException {
        String sql = "INSERT INTO products (name, description, price, category, image_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDouble(3, price);
            ps.setString(4, category != null && !category.isEmpty() ? category : "General");
            ps.setString(5, imageUrl != null && !imageUrl.isEmpty() ? imageUrl : "default_product.png");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        return new ProductDTO(id, name, description, price, category, imageUrl);
                    }
                }
            }
        }
        return null;
    }

    public ProductDTO getProductById(int productId) throws SQLException {
        String sql = "SELECT product_id, name, description, price, category, image_url FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ProductDTO(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("category"),
                            rs.getString("image_url")
                    );
                }
            }
        }
        return null;
    }
}
