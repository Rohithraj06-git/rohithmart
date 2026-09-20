package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.CartItem;
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

class CartItemDAOTest {

    private static HikariDataSource dataSource;
    private CartItemDAO cartItemDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;

    private long buyerId;
    private long productId;

    @BeforeAll
    static void setUpClass() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:cart_dao_test;DB_CLOSE_DELAY=-1");
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
        cartItemDAO = new CartItemDAOImpl();
        userDAO = new UserDAOImpl();
        productDAO = new ProductDAOImpl();

        DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(dataSource);
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        migrationManager.migrate();

        User seller = userDAO.create(new User(null, "Seller", "seller_c@test.com", "hash", Role.SELLER, null));
        User buyer = userDAO.create(new User(null, "Buyer", "buyer_c@test.com", "hash", Role.BUYER, null));
        buyerId = buyer.getId();

        Product prod = productDAO.create(new Product(null, seller.getId(), "Cart Prod", "Desc", new BigDecimal("25.00"), 50, "Electronics", null, null));
        productId = prod.getId();
    }

    @Test
    @DisplayName("CartItemDAO supports adding items, updating quantity, and clearing cart")
    void testCartLifecycle() throws SQLException {
        CartItem item = new CartItem(null, buyerId, productId, 2, null);
        CartItem created = cartItemDAO.create(item);
        assertNotNull(created.getId());

        // Find by user
        List<CartItem> items = cartItemDAO.findByUserId(buyerId);
        assertEquals(1, items.size());
        assertEquals(productId, items.get(0).getProductId());
        assertEquals(2, items.get(0).getQuantity());

        // Update quantity
        boolean updated = cartItemDAO.updateQuantity(created.getId(), 4);
        assertTrue(updated);

        Optional<CartItem> reloaded = cartItemDAO.findByUserAndProduct(buyerId, productId);
        assertTrue(reloaded.isPresent());
        assertEquals(4, reloaded.get().getQuantity());

        // Clear cart in transaction
        try (Connection conn = dataSource.getConnection()) {
            int cleared = cartItemDAO.clearCart(conn, buyerId);
            assertEquals(1, cleared);
        }

        List<CartItem> emptyCart = cartItemDAO.findByUserId(buyerId);
        assertTrue(emptyCart.isEmpty());
    }
}
