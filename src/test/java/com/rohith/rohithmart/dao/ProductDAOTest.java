package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Product;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.DatabaseMigrationManager;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductDAOTest {

    private static HikariDataSource dataSource;
    private ProductDAO productDAO;
    private UserDAO userDAO;
    private long testSellerId;

    @BeforeAll
    static void setUpClass() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:prod_dao_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
        DatabaseUtil.setDataSource(dataSource);
    }

    @AfterAll
    static void tearDownClass() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        productDAO = new ProductDAOImpl();
        userDAO = new UserDAOImpl();

        DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(dataSource);
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        migrationManager.migrate();

        // Create a test seller
        User seller = new User();
        seller.setName("Product Test Seller");
        seller.setEmail("prod_seller@example.com");
        seller.setPasswordHash("$2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG");
        seller.setRole(Role.SELLER);
        User createdSeller = userDAO.create(seller);
        testSellerId = createdSeller.getId();
    }

    @Test
    @DisplayName("ProductDAO can create, find, update, and delete products")
    void testProductCrud() throws SQLException {
        Product p = new Product();
        p.setSellerId(testSellerId);
        p.setName("Wireless Noise-Cancelling Headphones");
        p.setDescription("High fidelity studio audio");
        p.setPrice(new BigDecimal("199.99"));
        p.setStockQty(50);
        p.setCategory("Electronics");
        p.setImageUrl("https://example.com/headphones.jpg");

        Product created = productDAO.create(p);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);

        // Find by ID
        Optional<Product> found = productDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Wireless Noise-Cancelling Headphones", found.get().getName());
        assertEquals(new BigDecimal("199.99"), found.get().getPrice());
        assertEquals(50, found.get().getStockQty());

        // Update
        found.get().setName("Updated Wireless Headphones");
        found.get().setPrice(new BigDecimal("179.99"));
        found.get().setStockQty(45);
        boolean updated = productDAO.update(found.get());
        assertTrue(updated);

        Optional<Product> reloaded = productDAO.findById(created.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("Updated Wireless Headphones", reloaded.get().getName());
        assertEquals(new BigDecimal("179.99"), reloaded.get().getPrice());
        assertEquals(45, reloaded.get().getStockQty());

        // Delete
        boolean deleted = productDAO.delete(created.getId());
        assertTrue(deleted);
        assertTrue(productDAO.findById(created.getId()).isEmpty());
    }

    @Test
    @DisplayName("ProductDAO search supports filtering by category, keyword, and price sorting")
    void testProductSearchAndSort() throws SQLException {
        Product p1 = new Product(null, testSellerId, "Gaming Laptop", "Fast GPU", new BigDecimal("1200.00"), 10, "Electronics", null, null);
        Product p2 = new Product(null, testSellerId, "Office Chair", "Ergonomic mesh", new BigDecimal("150.00"), 20, "Furniture", null, null);
        Product p3 = new Product(null, testSellerId, "Mechanical Keyboard", "RGB switches", new BigDecimal("99.00"), 30, "Electronics", null, null);

        productDAO.create(p1);
        productDAO.create(p2);
        productDAO.create(p3);

        // Filter by category
        List<Product> electronics = productDAO.search("Electronics", null, null, 10, 0);
        assertEquals(2, electronics.size());

        // Search by keyword
        List<Product> searchResults = productDAO.search(null, "Laptop", null, 10, 0);
        assertEquals(1, searchResults.size());
        assertEquals("Gaming Laptop", searchResults.get(0).getName());

        // Sort by price ascending
        List<Product> priceAsc = productDAO.search("Electronics", null, "price_asc", 10, 0);
        assertEquals(2, priceAsc.size());
        assertEquals("Mechanical Keyboard", priceAsc.get(0).getName());
        assertEquals("Gaming Laptop", priceAsc.get(1).getName());
    }

    @Test
    @DisplayName("ProductDAO deductStock atomically decreases stock within a transaction")
    void testDeductStock() throws SQLException {
        Product p = new Product(null, testSellerId, "Stock Test Item", "Desc", new BigDecimal("10.00"), 15, "Electronics", null, null);
        Product created = productDAO.create(p);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            boolean success = productDAO.deductStock(conn, created.getId(), 5);
            assertTrue(success);
            conn.commit();
        }

        Optional<Product> reloaded = productDAO.findById(created.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(10, reloaded.get().getStockQty());

        // Attempting to deduct more than available must fail
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            boolean failDeduct = productDAO.deductStock(conn, created.getId(), 20);
            assertFalse(failDeduct);
            conn.rollback();
        }

        // Stock should remain unchanged
        assertEquals(10, productDAO.findById(created.getId()).get().getStockQty());
    }
}
