package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Product;
import com.rohith.rohithmart.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ProductDAO.
 */
public class ProductDAOImpl implements ProductDAO {

    @Override
    public Optional<Product> findById(Long id) throws SQLException {
        String sql = "SELECT id, seller_id, name, description, price, stock_qty, category, image_url, created_at "
                + "FROM products WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Product> search(String category, String keyword, String sortBy, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, seller_id, name, description, price, stock_qty, category, image_url, created_at FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.trim().isEmpty()) {
            sql.append("AND category = ? ");
            params.add(category.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY price ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY price DESC ");
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY name ASC ");
        } else {
            sql.append("ORDER BY created_at DESC ");
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(offset >= 0 ? offset : 0);

        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Product> findByCategory(String category) throws SQLException {
        return findByCategory(category, 100, 0);
    }

    @Override
    public List<Product> findByCategory(String category, int limit, int offset) throws SQLException {
        return search(category, null, null, limit, offset);
    }

    @Override
    public List<Product> findWithFilters(com.rohith.rohithmart.dto.ProductFilter filter) throws SQLException {
        if (filter == null) {
            return search(null, null, null, 20, 0);
        }

        StringBuilder sql = new StringBuilder("SELECT id, seller_id, name, description, price, stock_qty, category, image_url, created_at FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (filter.getCategory() != null && !filter.getCategory().trim().isEmpty()) {
            sql.append("AND category = ? ");
            params.add(filter.getCategory().trim());
        }

        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            String term = "%" + filter.getKeyword().trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (filter.getMinPrice() != null) {
            sql.append("AND price >= ? ");
            params.add(filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            sql.append("AND price <= ? ");
            params.add(filter.getMaxPrice());
        }

        if (Boolean.TRUE.equals(filter.getInStockOnly())) {
            sql.append("AND stock_qty > 0 ");
        }

        if (filter.getMinRating() != null && filter.getMinRating() > 0) {
            sql.append("AND id IN (SELECT product_id FROM reviews GROUP BY product_id HAVING AVG(CAST(rating AS DOUBLE)) >= ?) ");
            params.add(filter.getMinRating());
        }

        if (filter.getSellerId() != null && filter.getSellerId() > 0) {
            sql.append("AND seller_id = ? ");
            params.add(filter.getSellerId());
        }

        String sortBy = filter.getSortBy();
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY price ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY price DESC ");
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY name ASC ");
        } else if ("name_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY name DESC ");
        } else {
            sql.append("ORDER BY created_at DESC ");
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(filter.getSize());
        params.add(filter.getOffset());

        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public int countFiltered(com.rohith.rohithmart.dto.ProductFilter filter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (filter != null) {
            if (filter.getCategory() != null && !filter.getCategory().trim().isEmpty()) {
                sql.append("AND category = ? ");
                params.add(filter.getCategory().trim());
            }
            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
                String term = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                params.add(term);
                params.add(term);
            }
            if (filter.getMinPrice() != null) {
                sql.append("AND price >= ? ");
                params.add(filter.getMinPrice());
            }
            if (filter.getMaxPrice() != null) {
                sql.append("AND price <= ? ");
                params.add(filter.getMaxPrice());
            }
            if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                sql.append("AND stock_qty > 0 ");
            }
            if (filter.getMinRating() != null && filter.getMinRating() > 0) {
                sql.append("AND id IN (SELECT product_id FROM reviews GROUP BY product_id HAVING AVG(CAST(rating AS DOUBLE)) >= ?) ");
                params.add(filter.getMinRating());
            }
            if (filter.getSellerId() != null && filter.getSellerId() > 0) {
                sql.append("AND seller_id = ? ");
                params.add(filter.getSellerId());
            }
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, seller_id, name, description, price, stock_qty, category, image_url, created_at "
                + "FROM products WHERE seller_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Product create(Product product) throws SQLException {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQty());
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getLong(1));
                }
            }
        }
        return product;
    }

    @Override
    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, category = ?, image_url = ? "
                + "WHERE id = ? AND seller_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQty());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getId());
            ps.setLong(8, product.getSellerId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deductStock(Connection conn, Long productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
