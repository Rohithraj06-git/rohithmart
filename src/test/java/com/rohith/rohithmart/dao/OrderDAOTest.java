package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Order;
import com.rohith.rohithmart.model.OrderItem;
import com.rohith.rohithmart.model.OrderStatus;
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

class OrderDAOTest {

    private static HikariDataSource dataSource;
    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;

    private long buyerId;
    private long productId;

    @BeforeAll
    static void setUpClass() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:order_dao_test;DB_CLOSE_DELAY=-1");
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
        orderDAO = new OrderDAOImpl();
        userDAO = new UserDAOImpl();
        productDAO = new ProductDAOImpl();

        DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(dataSource);
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        migrationManager.migrate();

        // Setup buyer and seller
        User seller = userDAO.create(new User(null, "Seller", "seller_o@test.com", "hash", Role.SELLER, null));
        User buyer = userDAO.create(new User(null, "Buyer", "buyer_o@test.com", "hash", Role.BUYER, null));
        buyerId = buyer.getId();

        Product prod = productDAO.create(new Product(null, seller.getId(), "Order Test Prod", "Desc", new BigDecimal("50.00"), 20, "Electronics", null, null));
        productId = prod.getId();
    }

    @Test
    @DisplayName("OrderDAO creates order header and order items within a transaction")
    void testCreateOrderAndItemsTransaction() throws SQLException {
        Order createdOrder;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            Order order = new Order();
            order.setBuyerId(buyerId);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setTotalAmount(new BigDecimal("100.00"));
            createdOrder = orderDAO.createOrder(conn, order);

            OrderItem item1 = new OrderItem(null, createdOrder.getId(), productId, 2, new BigDecimal("50.00"), null);
            orderDAO.createOrderItem(conn, item1);

            conn.commit();
        }

        assertNotNull(createdOrder.getId());

        // Verify findById
        Optional<Order> found = orderDAO.findById(createdOrder.getId());
        assertTrue(found.isPresent());
        assertEquals(buyerId, found.get().getBuyerId());
        assertEquals(OrderStatus.CONFIRMED, found.get().getStatus());
        assertEquals(new BigDecimal("100.00"), found.get().getTotalAmount());

        // Verify findItemsByOrderId
        List<OrderItem> items = orderDAO.findItemsByOrderId(createdOrder.getId());
        assertEquals(1, items.size());
        assertEquals(productId, items.get(0).getProductId());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(new BigDecimal("50.00"), items.get(0).getUnitPrice());

        // Update status
        boolean updated = orderDAO.updateStatus(createdOrder.getId(), OrderStatus.SHIPPED);
        assertTrue(updated);
        assertEquals(OrderStatus.SHIPPED, orderDAO.findById(createdOrder.getId()).get().getStatus());
    }

    @Test
    @DisplayName("OrderDAO findByBuyerId returns orders for specified buyer")
    void testFindByBuyerId() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            Order o1 = new Order(null, buyerId, OrderStatus.DELIVERED, new BigDecimal("35.00"), null);
            orderDAO.createOrder(conn, o1);
        }

        List<Order> buyerOrders = orderDAO.findByBuyerId(buyerId);
        assertEquals(1, buyerOrders.size());
        assertEquals(new BigDecimal("35.00"), buyerOrders.get(0).getTotalAmount());
    }
}
