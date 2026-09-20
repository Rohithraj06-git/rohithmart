package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.OrderItem;
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
 * JDBC implementation of OrderItemDAO.
 */
public class OrderItemDAOImpl implements OrderItemDAO {

    @Override
    public OrderItem create(Connection conn, OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getOrderId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }
        }
        return item;
    }

    @Override
    public OrderItem create(OrderItem item) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            return create(conn, item);
        }
    }

    @Override
    public void createBatch(Connection conn, List<OrderItem> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : items) {
                ps.setLong(1, item.getOrderId());
                ps.setLong(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;
                while (rs.next() && i < items.size()) {
                    items.get(i).setId(rs.getLong(1));
                    i++;
                }
            }
        }
    }

    @Override
    public Optional<OrderItem> findById(Long id) throws SQLException {
        String sql = "SELECT id, order_id, product_id, quantity, unit_price, created_at FROM order_items WHERE id = ?";
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
    public List<OrderItem> findByOrderId(Long orderId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            return findByOrderId(conn, orderId);
        }
    }

    @Override
    public List<OrderItem> findByOrderId(Connection conn, Long orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT id, order_id, product_id, quantity, unit_price, created_at FROM order_items WHERE order_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<OrderItem> findBySellerId(Long sellerId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.created_at "
                + "FROM order_items oi "
                + "JOIN products p ON oi.product_id = p.id "
                + "WHERE p.seller_id = ? "
                + "ORDER BY oi.created_at DESC";
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

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        return item;
    }
}
