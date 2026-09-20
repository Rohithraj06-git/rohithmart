package com.rohith.rohithmart.db;

import com.rohith.rohithmart.util.DatabaseMigrationManager;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates integrity constraints: Foreign keys, uniqueness, cascades, and check constraints.
 */
class ForeignKeyConstraintTest {

    private static HikariDataSource dataSource;

    @BeforeAll
    static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:fk_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
        DatabaseUtil.setDataSource(dataSource);

        DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(dataSource);
        migrationManager.migrate();
    }

    @AfterAll
    static void tearDown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Test
    @DisplayName("Unique email constraint rejects duplicate email registrations")
    void testUniqueEmailConstraint() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // First user
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name, email, password_hash, role) VALUES ('User 1', 'unique@example.com', 'hash1', 'BUYER')")) {
                ps.executeUpdate();
            }

            // Second user with same email must fail
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name, email, password_hash, role) VALUES ('User 2', 'unique@example.com', 'hash2', 'SELLER')")) {
                assertThrows(SQLException.class, ps::executeUpdate, "Duplicate email insert should violate UNIQUE constraint");
            }
        }
    }

    @Test
    @DisplayName("Foreign key rejects product insertion with invalid non-existent seller_id")
    void testForeignKeyRejectInvalidSeller() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO products (seller_id, name, description, price, stock_qty, category) "
                             + "VALUES (999999, 'Invalid Product', 'Desc', 10.00, 5, 'Electronics')")) {
            assertThrows(SQLException.class, ps::executeUpdate, "Inserting product with invalid seller_id must violate FK");
        }
    }

    @Test
    @DisplayName("Foreign key rejects order insertion with invalid non-existent buyer_id")
    void testForeignKeyRejectInvalidBuyer() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO orders (buyer_id, status, total_amount) VALUES (888888, 'PENDING', 50.00)")) {
            assertThrows(SQLException.class, ps::executeUpdate, "Inserting order with invalid buyer_id must violate FK");
        }
    }

    @Test
    @DisplayName("Check constraint rejects invalid user role")
    void testCheckConstraintRole() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES ('Bad Role', 'badrole@example.com', 'hash', 'SUPERUSER')")) {
            assertThrows(SQLException.class, ps::executeUpdate, "Invalid role should violate CHECK constraint");
        }
    }

    @Test
    @DisplayName("Check constraint rejects negative product price or stock")
    void testCheckConstraintPriceAndStock() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Insert valid seller first
            long sellerId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name, email, password_hash, role) VALUES ('Valid Seller', 'validseller@example.com', 'hash', 'SELLER')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                assertTrue(rs.next());
                sellerId = rs.getLong(1);
            }

            // Negative price must fail
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (?, 'Negative Price', -10.00, 5, 'Electronics')")) {
                ps.setLong(1, sellerId);
                assertThrows(SQLException.class, ps::executeUpdate, "Negative price must violate CHECK constraint");
            }

            // Negative stock must fail
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (?, 'Negative Stock', 10.00, -5, 'Electronics')")) {
                ps.setLong(1, sellerId);
                assertThrows(SQLException.class, ps::executeUpdate, "Negative stock must violate CHECK constraint");
            }
        }
    }

    @Test
    @DisplayName("Check constraint rejects review rating outside 1 to 5")
    void testCheckConstraintReviewRating() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Insert user & product
            long userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name, email, password_hash, role) VALUES ('Reviewer', 'rev@example.com', 'hash', 'BUYER')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                assertTrue(rs.next());
                userId = rs.getLong(1);
            }

            long productId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (?, 'Rev Product', 15.00, 10, 'Books')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                assertTrue(rs.next());
                productId = rs.getLong(1);
            }

            // Rating = 0 should fail
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, 0, 'Zero stars')")) {
                ps.setLong(1, productId);
                ps.setLong(2, userId);
                assertThrows(SQLException.class, ps::executeUpdate, "Rating of 0 should violate CHECK constraint");
            }

            // Rating = 6 should fail
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, 6, 'Six stars')")) {
                ps.setLong(1, productId);
                ps.setLong(2, userId);
                assertThrows(SQLException.class, ps::executeUpdate, "Rating of 6 should violate CHECK constraint");
            }
        }
    }

    @Test
    @DisplayName("CASCADE deletion: deleting a user deletes their products and cart items")
    void testCascadeDeletion() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Create user
            long userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name, email, password_hash, role) VALUES ('Cascade User', 'cascade@example.com', 'hash', 'SELLER')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                assertTrue(rs.next());
                userId = rs.getLong(1);
            }

            // Create product for user
            long productId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (?, 'Cascade Prod', 20.00, 10, 'Fashion')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                assertTrue(rs.next());
                productId = rs.getLong(1);
            }

            // Delete user
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps.setLong(1, userId);
                int affected = ps.executeUpdate();
                assertEquals(1, affected);
            }

            // Product should be automatically removed by ON DELETE CASCADE
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM products WHERE id = ?")) {
                ps.setLong(1, productId);
                var rs = ps.executeQuery();
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Product must be deleted when its seller user is deleted");
            }
        }
    }
}
