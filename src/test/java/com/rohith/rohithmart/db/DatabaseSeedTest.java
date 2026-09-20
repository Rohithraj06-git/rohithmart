package com.rohith.rohithmart.db;

import com.rohith.rohithmart.util.DatabaseMigrationManager;
import com.rohith.rohithmart.util.PasswordUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates development seed data, password hashing compliance, and relational consistency.
 */
class DatabaseSeedTest {

    private static HikariDataSource dataSource;

    @BeforeAll
    static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:seed_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);

        DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(dataSource);
        migrationManager.migrate();
        migrationManager.seedIfEmpty();
    }

    @AfterAll
    static void tearDown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Test
    @DisplayName("Verify seed users exist with roles: Admin, Sellers, and Buyers")
    void testSeedUsers() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT role, COUNT(*) as cnt FROM users GROUP BY role");
             ResultSet rs = ps.executeQuery()) {

            int adminCount = 0;
            int sellerCount = 0;
            int buyerCount = 0;

            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("cnt");
                if ("ADMIN".equals(role)) adminCount = count;
                if ("SELLER".equals(role)) sellerCount = count;
                if ("BUYER".equals(role)) buyerCount = count;
            }

            assertEquals(1, adminCount, "Must have exactly 1 seeded admin");
            assertTrue(sellerCount >= 1, "Must have at least 1 seeded seller");
            assertTrue(buyerCount >= 1, "Must have at least 1 seeded buyer");
        }
    }

    @Test
    @DisplayName("Verify that all seed users have BCrypt password hashes matching Password@123")
    void testSeedPasswordsAreBcrypt() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT email, password_hash FROM users");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String email = rs.getString("email");
                String hash = rs.getString("password_hash");

                assertNotNull(hash);
                assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"), "Hash must be a valid BCrypt hash for " + email);
                assertTrue(PasswordUtil.verifyPassword("Password@123", hash), "Password@123 must verify against hash for " + email);
            }
        }
    }

    @Test
    @DisplayName("Verify seeded products span multiple categories with positive stock and price")
    void testSeedProducts() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name, category, price, stock_qty FROM products");
             ResultSet rs = ps.executeQuery()) {

            Set<String> categories = new HashSet<>();
            int totalProducts = 0;

            while (rs.next()) {
                totalProducts++;
                categories.add(rs.getString("category"));
                BigDecimal price = rs.getBigDecimal("price");
                int stock = rs.getInt("stock_qty");

                assertTrue(price.compareTo(BigDecimal.ZERO) > 0, "Price must be positive");
                assertTrue(stock >= 0, "Stock quantity must be non-negative");
            }

            assertTrue(totalProducts >= 5, "Should have multiple seeded products");
            assertTrue(categories.size() >= 3, "Products must span at least 3 categories");
            assertTrue(categories.contains("Electronics"));
            assertTrue(categories.contains("Fashion"));
        }
    }

    @Test
    @DisplayName("Verify seeded orders and items are properly linked")
    void testSeedOrdersAndItems() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Check orders count
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM orders");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) > 0, "Should have seeded orders");
            }

            // Check order_items count
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM order_items");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) > 0, "Should have seeded order items");
            }

            // Verify order items link to existing products and orders
            String verifyJoinSql = "SELECT oi.id, o.id as order_id, p.name as product_name "
                    + "FROM order_items oi "
                    + "JOIN orders o ON oi.order_id = o.id "
                    + "JOIN products p ON oi.product_id = p.id";
            try (PreparedStatement ps = conn.prepareStatement(verifyJoinSql);
                 ResultSet rs = ps.executeQuery()) {
                int linkedCount = 0;
                while (rs.next()) {
                    linkedCount++;
                    assertNotNull(rs.getString("product_name"));
                }
                assertTrue(linkedCount > 0, "Order items must join cleanly to orders and products");
            }
        }
    }

    @Test
    @DisplayName("Verify seeded reviews contain valid ratings between 1 and 5")
    void testSeedReviews() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT rating, comment FROM reviews");
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                count++;
                int rating = rs.getInt("rating");
                assertTrue(rating >= 1 && rating <= 5, "Rating must be between 1 and 5");
                assertNotNull(rs.getString("comment"), "Review comment should not be null");
            }
            assertTrue(count > 0, "Should have seeded reviews");
        }
    }
}
