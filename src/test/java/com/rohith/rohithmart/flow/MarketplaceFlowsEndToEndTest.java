package com.rohith.rohithmart.flow;

import com.rohith.rohithmart.dto.*;
import com.rohith.rohithmart.exception.ConflictException;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.OrderStatus;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.*;
import com.rohith.rohithmart.util.DatabaseMigrationManager;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive End-to-End Business Flow & Security Audit Test.
 * Validates:
 * 1. BUYER FLOW: Register -> Login -> Browse/Search/Filter/Sort -> Details -> Cart -> Checkout -> History -> Review
 * 2. SELLER FLOW: Login -> Dashboard stats -> Product CRUD -> Stock management -> Order tracking -> Status update -> IDOR prevention
 * 3. ADMIN FLOW: Login -> Platform stats -> User management -> Role promotion -> Listing moderation
 */
@DisplayName("Marketplace End-to-End Business Flows & Security Audit Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MarketplaceFlowsEndToEndTest {

    private static HikariDataSource dataSource;
    private static AuthService authService;
    private static ProductService productService;
    private static CartService cartService;
    private static OrderService orderService;
    private static ReviewService reviewService;
    private static UserService userService;

    private static Long buyerId;
    private static Long sellerId;
    private static Long secondSellerId;
    private static Long adminId;
    private static Long testProductId;
    private static Long testOrderId;

    @BeforeAll
    static void initDatabase() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:flow_audit_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
        DatabaseUtil.setDataSource(dataSource);

        DatabaseMigrationManager manager = new DatabaseMigrationManager(dataSource);
        manager.migrate();
        manager.seedIfEmpty();

        authService = ServiceFactory.getAuthService();
        productService = ServiceFactory.getProductService();
        cartService = ServiceFactory.getCartService();
        orderService = ServiceFactory.getOrderService();
        reviewService = ServiceFactory.getReviewService();
        userService = ServiceFactory.getUserService();
    }

    @AfterAll
    static void tearDownDatabase() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("BUYER FLOW 1: Registration & Authentication with BCrypt Verification")
    void testBuyerRegistrationAndLogin() throws Exception {
        // 1. Register new buyer
        RegisterRequestDTO regDto = new RegisterRequestDTO(
                "Audit Buyer",
                "audit.buyer@rohithmart.com",
                "Password@123",
                Role.BUYER
        );

        UserResponseDTO buyer = authService.register(regDto);
        assertNotNull(buyer);
        assertNotNull(buyer.getId());
        assertEquals("Audit Buyer", buyer.getName());
        assertEquals(Role.BUYER, buyer.getRole());
        buyerId = buyer.getId();

        // 2. Prevent duplicate email registration
        assertThrows(ConflictException.class, () -> authService.register(regDto));

        // 3. Authenticate with valid credentials
        LoginRequestDTO loginDto = new LoginRequestDTO("audit.buyer@rohithmart.com", "Password@123");
        User loggedIn = authService.login(loginDto);
        assertNotNull(loggedIn);
        assertEquals(buyerId, loggedIn.getId());

        // 4. Reject invalid password
        LoginRequestDTO badLogin = new LoginRequestDTO("audit.buyer@rohithmart.com", "WrongPassword@123");
        assertThrows(Exception.class, () -> authService.login(badLogin));
    }

    @Test
    @Order(2)
    @DisplayName("BUYER FLOW 2: Catalog Browsing, Multi-Criteria Search, Filtering & Sorting")
    void testBuyerCatalogBrowseSearchFilterSort() throws Exception {
        // 1. Browse with filters
        ProductFilter filter = ProductFilter.builder()
                .category("Electronics")
                .minPrice(new BigDecimal("10.00"))
                .maxPrice(new BigDecimal("2000.00"))
                .inStockOnly(true)
                .sortBy("price_asc")
                .page(1)
                .size(10)
                .build();

        PagedResponse<ProductDTO> paged = productService.searchProductsWithFilters(filter);
        assertNotNull(paged);
        assertFalse(paged.getData().isEmpty());
        assertTrue(paged.getTotalResults() > 0);

        // Verify sorted price ascending
        List<ProductDTO> items = paged.getData();
        for (int i = 1; i < items.size(); i++) {
            assertTrue(items.get(i).getPrice().compareTo(items.get(i - 1).getPrice()) >= 0);
        }

        // 2. Search by keyword
        ProductFilter keywordFilter = ProductFilter.builder()
                .keyword("Laptop")
                .page(1)
                .size(10)
                .build();
        PagedResponse<ProductDTO> searchResult = productService.searchProductsWithFilters(keywordFilter);
        assertNotNull(searchResult);
        assertFalse(searchResult.getData().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("BUYER FLOW 3: Product Detail, Cart Management & Server-Side Stock Bounds")
    void testBuyerCartAndStockValidation() throws Exception {
        // 1. Load details for product ID 1
        ProductDTO product = productService.getProductById(1L);
        assertNotNull(product);
        int availableStock = product.getStockQty();
        assertTrue(availableStock > 0);

        // 2. Add product to cart
        cartService.addToCart(buyerId, 1L, 2);
        List<CartItemDTO> cartItems = cartService.getCartItems(buyerId);
        assertFalse(cartItems.isEmpty());
        CartItemDTO cartItem = cartItems.get(0);
        assertEquals(2, cartItem.getQuantity());

        // 3. Update quantity validly
        cartService.updateQuantity(cartItem.getId(), buyerId, 3);
        List<CartItemDTO> updatedItems = cartService.getCartItems(buyerId);
        assertEquals(3, updatedItems.get(0).getQuantity());

        // 4. Server authoritative price calculation
        BigDecimal computedTotal = cartService.calculateTotal(updatedItems);
        assertEquals(product.getPrice().multiply(BigDecimal.valueOf(3)), computedTotal);

        // 5. Reject quantity exceeding warehouse stock
        assertThrows(ValidationException.class, () ->
                cartService.updateQuantity(cartItem.getId(), buyerId, availableStock + 100)
        );
    }

    @Test
    @Order(4)
    @DisplayName("BUYER FLOW 4: Atomic Checkout Transaction & Order History")
    void testBuyerAtomicCheckoutAndOrderHistory() throws Exception {
        ProductDTO prodBefore = productService.getProductById(1L);
        int initialStock = prodBefore.getStockQty();

        // 1. Execute atomic checkout
        OrderDTO order = orderService.checkout(buyerId);
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertFalse(order.getItems().isEmpty());
        testOrderId = order.getId();

        // 2. Verify stock deduction
        ProductDTO prodAfter = productService.getProductById(1L);
        assertEquals(initialStock - 3, prodAfter.getStockQty());

        // 3. Verify cart is cleared
        List<CartItemDTO> cartAfter = cartService.getCartItems(buyerId);
        assertTrue(cartAfter.isEmpty());

        // 4. Verify order history
        List<OrderDTO> history = orderService.getBuyerOrders(buyerId);
        assertFalse(history.isEmpty());
        assertEquals(testOrderId, history.get(0).getId());
    }

    @Test
    @Order(5)
    @DisplayName("BUYER FLOW 5: Verified Customer Review Submission & Duplicate Prevention")
    void testBuyerReviewSubmission() throws Exception {
        // 1. Submit review
        ReviewDTO review = reviewService.addReview(1L, buyerId, 5, "Outstanding build quality and fast delivery!");
        assertNotNull(review);
        assertEquals(5, review.getRating());
        assertEquals(1L, review.getProductId());
        assertEquals(buyerId, review.getUserId());

        // 2. Verify duplicate review prevention (unique constraint / conflict check)
        assertThrows(ConflictException.class, () ->
                reviewService.addReview(1L, buyerId, 4, "Duplicate review attempt")
        );

        // 3. Verify review appears in product review list
        List<ReviewDTO> reviews = reviewService.getReviewsByProduct(1L);
        assertTrue(reviews.stream().anyMatch(r -> r.getUserId().equals(buyerId)));
    }

    @Test
    @Order(6)
    @DisplayName("SELLER FLOW 1: Seller Authentication, Dashboard Statistics & Metrics")
    void testSellerAuthenticationAndDashboard() throws Exception {
        // Login as existing seed seller
        LoginRequestDTO loginDto = new LoginRequestDTO("techseller@rohithmart.com", "Password@123");
        User seller = authService.login(loginDto);
        assertNotNull(seller);
        assertEquals(Role.SELLER, seller.getRole());
        sellerId = seller.getId();

        // Query seller listings
        List<ProductDTO> listings = productService.getProductsBySeller(sellerId);
        assertFalse(listings.isEmpty());

        // Query seller relevant orders
        List<OrderDTO> sellerOrders = orderService.getSellerOrders(sellerId);
        assertNotNull(sellerOrders);
        assertFalse(sellerOrders.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("SELLER FLOW 2: Product Creation, Editing, and IDOR Defense")
    void testSellerProductCrudAndIdorDefense() throws Exception {
        // 1. Create a second seller to test cross-seller isolation
        RegisterRequestDTO s2Dto = new RegisterRequestDTO(
                "Second Seller",
                "seller2@rohithmart.com",
                "Password@123",
                Role.SELLER
        );
        UserResponseDTO seller2 = authService.register(s2Dto);
        secondSellerId = seller2.getId();

        // 2. Seller 1 creates product
        ProductDTO newProd = ProductDTO.builder()
                .name("Audit Wireless Headphones")
                .description("Noise cancelling bluetooth over-ear headphones")
                .category("Electronics")
                .price(new BigDecimal("99.99"))
                .stockQty(15)
                .imageUrl("https://example.com/headphones.jpg")
                .build();

        ProductDTO created = productService.createProduct(newProd, sellerId);
        assertNotNull(created.getId());
        testProductId = created.getId();

        // 3. Seller 1 updates own product
        created.setPrice(new BigDecimal("89.99"));
        created.setStockQty(20);
        ProductDTO updated = productService.updateProduct(created, sellerId, false);
        assertEquals(new BigDecimal("89.99"), updated.getPrice());
        assertEquals(20, updated.getStockQty());

        // 4. IDOR Defense: Seller 2 CANNOT update Seller 1's product
        assertThrows(ForbiddenException.class, () ->
                productService.updateProduct(created, secondSellerId, false)
        );

        // 5. IDOR Defense: Seller 2 CANNOT delete Seller 1's product
        assertThrows(ForbiddenException.class, () ->
                productService.deleteProduct(testProductId, secondSellerId, false)
        );
    }

    @Test
    @Order(8)
    @DisplayName("SELLER FLOW 3: Order Fulfillment Status Update & IDOR Protection")
    void testSellerOrderStatusUpdateAndIdorProtection() throws Exception {
        // Order #testOrderId contains product from sellerId
        // 1. Permitted status transition: CONFIRMED -> SHIPPED
        assertDoesNotThrow(() ->
                orderService.updateOrderStatus(testOrderId, OrderStatus.SHIPPED, sellerId, false)
        );
        OrderDTO updated = orderService.getOrderById(testOrderId, buyerId, false);
        assertEquals(OrderStatus.SHIPPED, updated.getStatus());

        // 2. Permitted status transition: SHIPPED -> DELIVERED
        assertDoesNotThrow(() ->
                orderService.updateOrderStatus(testOrderId, OrderStatus.DELIVERED, sellerId, false)
        );
        OrderDTO delivered = orderService.getOrderById(testOrderId, buyerId, false);
        assertEquals(OrderStatus.DELIVERED, delivered.getStatus());

        // 3. IDOR Protection: Seller 2 (who has NO items in this order) CANNOT update status
        assertThrows(ForbiddenException.class, () ->
                orderService.updateOrderStatus(testOrderId, OrderStatus.CONFIRMED, secondSellerId, false)
        );
    }

    @Test
    @Order(9)
    @DisplayName("ADMIN FLOW 1: Admin Login, Platform Metrics & User Role Management")
    void testAdminDashboardAndUserManagement() throws Exception {
        // 1. Admin login
        LoginRequestDTO loginDto = new LoginRequestDTO("admin@rohithmart.com", "Password@123");
        User admin = authService.login(loginDto);
        assertNotNull(admin);
        assertEquals(Role.ADMIN, admin.getRole());
        adminId = admin.getId();

        // 2. Query platform-wide statistics
        int totalUsers = userService.getTotalUserCount();
        int totalProducts = productService.getTotalProductCount();
        int totalOrders = orderService.getTotalOrderCount();
        assertTrue(totalUsers >= 3);
        assertTrue(totalProducts >= 5);
        assertTrue(totalOrders >= 1);

        // 3. User management: Promote a buyer to seller
        userService.updateUserRole(adminId, buyerId, Role.SELLER);
        UserResponseDTO updatedBuyer = userService.getUserById(buyerId);
        assertEquals(Role.SELLER, updatedBuyer.getRole());
    }

    @Test
    @Order(10)
    @DisplayName("ADMIN FLOW 2: Admin Override Moderation of Listings")
    void testAdminListingModeration() throws Exception {
        // Admin can administratively delete any inappropriate listing
        assertDoesNotThrow(() ->
                productService.deleteProduct(testProductId, adminId, true)
        );

        // Verify product is no longer found
        assertThrows(Exception.class, () ->
                productService.getProductById(testProductId)
        );
    }
}
