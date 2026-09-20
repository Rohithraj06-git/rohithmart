package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Product;
import com.rohith.rohithmart.model.Review;
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

import static org.junit.jupiter.api.Assertions.*;

class ReviewDAOTest {

    private static HikariDataSource dataSource;
    private ReviewDAO reviewDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;

    private long buyerId;
    private long productId;

    @BeforeAll
    static void setUpClass() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:review_dao_test;DB_CLOSE_DELAY=-1");
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
        reviewDAO = new ReviewDAOImpl();
        userDAO = new UserDAOImpl();
        productDAO = new ProductDAOImpl();

        DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(dataSource);
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        migrationManager.migrate();

        User seller = userDAO.create(new User(null, "Seller", "seller_r@test.com", "hash", Role.SELLER, null));
        User buyer = userDAO.create(new User(null, "Buyer", "buyer_r@test.com", "hash", Role.BUYER, null));
        buyerId = buyer.getId();

        Product prod = productDAO.create(new Product(null, seller.getId(), "Review Prod", "Desc", new BigDecimal("40.00"), 10, "Fashion", null, null));
        productId = prod.getId();
    }

    @Test
    @DisplayName("ReviewDAO creates reviews and queries by product ID")
    void testCreateAndFindReviews() throws SQLException {
        Review review = new Review(null, productId, buyerId, 5, "Remarkable craftsmanship and fit!", null);
        Review created = reviewDAO.create(review);
        assertNotNull(created.getId());

        List<Review> list = reviewDAO.findByProductId(productId);
        assertEquals(1, list.size());
        assertEquals(5, list.get(0).getRating());
        assertEquals("Remarkable craftsmanship and fit!", list.get(0).getComment());
        assertEquals(buyerId, list.get(0).getUserId());
    }
}
