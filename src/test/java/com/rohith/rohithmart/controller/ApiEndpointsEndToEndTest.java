package com.rohith.rohithmart.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rohith.rohithmart.util.DatabaseMigrationManager;
import com.rohith.rohithmart.util.JsonUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * End-to-End manual verification of important API endpoints executing through
 * pure HTTP -> Servlet -> Service -> DAO against real H2 database.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("API Endpoints End-To-End Integration Verification")
public class ApiEndpointsEndToEndTest {

    private static HikariDataSource dataSource;

    @BeforeAll
    static void initDatabase() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:e2e_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
        com.rohith.rohithmart.util.DatabaseUtil.setDataSource(dataSource);
        DatabaseMigrationManager manager = new DatabaseMigrationManager(dataSource);
        manager.migrate();
        manager.seedIfEmpty();
    }

    @AfterAll
    static void tearDownDatabase() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static class MockSessionState {
        Map<String, Object> attributes = new HashMap<>();
        int maxInactiveInterval = 0;
        boolean invalidated = false;

        HttpSession createMock() {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute(anyString())).thenAnswer(inv -> attributes.get(inv.getArgument(0)));
            doAnswer(inv -> {
                attributes.put(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(session).setAttribute(anyString(), any());
            doAnswer(inv -> {
                maxInactiveInterval = inv.getArgument(0);
                return null;
            }).when(session).setMaxInactiveInterval(anyInt());
            when(session.getMaxInactiveInterval()).thenAnswer(inv -> maxInactiveInterval);
            doAnswer(inv -> {
                invalidated = true;
                attributes.clear();
                return null;
            }).when(session).invalidate();
            return session;
        }
    }

    private static class ServletExecutionResult {
        int statusCode = 200;
        String contentType;
        StringWriter output = new StringWriter();

        HttpServletResponse createResponseMock() throws Exception {
            HttpServletResponse res = mock(HttpServletResponse.class);
            doAnswer(inv -> {
                statusCode = inv.getArgument(0);
                return null;
            }).when(res).setStatus(anyInt());
            doAnswer(inv -> {
                contentType = inv.getArgument(0);
                return null;
            }).when(res).setContentType(anyString());
            when(res.getWriter()).thenReturn(new PrintWriter(output));
            return res;
        }

        JsonObject getJson() {
            return JsonUtil.fromJson(output.toString(), JsonObject.class);
        }
    }

    @Test
    @DisplayName("Verify Full Lifecycle: Login -> Search Products -> Cart -> Checkout -> Review -> Chat")
    void testFullApiLifecycle() throws Exception {
        MockSessionState sessionState = new MockSessionState();
        HttpSession session = sessionState.createMock();

        // 1. POST /api/auth/login as John Buyer
        AuthApiServlet authServlet = new AuthApiServlet();
        HttpServletRequest loginReq = mock(HttpServletRequest.class);
        when(loginReq.getRequestURI()).thenReturn("/api/auth/login");
        when(loginReq.getPathInfo()).thenReturn("/login");
        when(loginReq.getContentType()).thenReturn("application/json");
        when(loginReq.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"email\":\"john.buyer@example.com\",\"password\":\"Password@123\"}"
        )));
        when(loginReq.getSession(true)).thenReturn(session);

        ServletExecutionResult loginRes = new ServletExecutionResult();
        authServlet.doPost(loginReq, loginRes.createResponseMock());

        assertEquals(200, loginRes.statusCode);
        JsonObject loginJson = loginRes.getJson();
        assertTrue(loginJson.get("success").getAsBoolean());
        JsonObject userData = loginJson.getAsJsonObject("data");
        assertEquals("john.buyer@example.com", userData.get("email").getAsString());
        assertEquals("BUYER", userData.get("role").getAsString());
        assertNotNull(sessionState.attributes.get("user"));
        assertNotNull(sessionState.attributes.get("_csrf_token"));

        // 2. GET /api/products with pagination, category filter and sorting
        ProductApiServlet productServlet = new ProductApiServlet();
        HttpServletRequest prodReq = mock(HttpServletRequest.class);
        when(prodReq.getRequestURI()).thenReturn("/api/products");
        when(prodReq.getPathInfo()).thenReturn(null);
        when(prodReq.getParameter("page")).thenReturn("1");
        when(prodReq.getParameter("size")).thenReturn("5");
        when(prodReq.getParameter("category")).thenReturn("Electronics");
        when(prodReq.getParameter("sort")).thenReturn("price_asc");

        ServletExecutionResult prodRes = new ServletExecutionResult();
        productServlet.doGet(prodReq, prodRes.createResponseMock());

        assertEquals(200, prodRes.statusCode);
        JsonObject prodJson = prodRes.getJson();
        assertTrue(prodJson.get("success").getAsBoolean());
        JsonObject pagedData = prodJson.getAsJsonObject("data");
        assertEquals(1, pagedData.get("currentPage").getAsInt());
        assertEquals(5, pagedData.get("pageSize").getAsInt());
        JsonArray items = pagedData.getAsJsonArray("data");
        assertTrue(items.size() > 0);

        // 3. GET /api/products/1 (Product detail)
        HttpServletRequest detailReq = mock(HttpServletRequest.class);
        when(detailReq.getRequestURI()).thenReturn("/api/products/1");
        when(detailReq.getPathInfo()).thenReturn("/1");

        ServletExecutionResult detailRes = new ServletExecutionResult();
        productServlet.doGet(detailReq, detailRes.createResponseMock());

        assertEquals(200, detailRes.statusCode);
        JsonObject detailJson = detailRes.getJson();
        assertTrue(detailJson.get("success").getAsBoolean());
        assertEquals(1L, detailJson.getAsJsonObject("data").get("id").getAsLong());

        // 4. POST /api/cart/add (Add product 2, quantity 2)
        CartApiServlet cartServlet = new CartApiServlet();
        HttpServletRequest addCartReq = mock(HttpServletRequest.class);
        when(addCartReq.getRequestURI()).thenReturn("/api/cart/add");
        when(addCartReq.getPathInfo()).thenReturn("/add");
        when(addCartReq.getContentType()).thenReturn("application/json");
        when(addCartReq.getSession(false)).thenReturn(session);
        when(addCartReq.getReader()).thenReturn(new BufferedReader(new StringReader("{\"productId\":2,\"quantity\":2}")));

        ServletExecutionResult addCartRes = new ServletExecutionResult();
        cartServlet.doPost(addCartReq, addCartRes.createResponseMock());

        assertEquals(201, addCartRes.statusCode);
        assertTrue(addCartRes.getJson().get("success").getAsBoolean());

        // 5. GET /api/cart (Verify cart contents and calculated total)
        HttpServletRequest getCartReq = mock(HttpServletRequest.class);
        when(getCartReq.getRequestURI()).thenReturn("/api/cart");
        when(getCartReq.getSession(false)).thenReturn(session);

        ServletExecutionResult getCartRes = new ServletExecutionResult();
        cartServlet.doGet(getCartReq, getCartRes.createResponseMock());

        assertEquals(200, getCartRes.statusCode);
        JsonObject cartJson = getCartRes.getJson();
        assertTrue(cartJson.get("success").getAsBoolean());
        JsonObject cartData = cartJson.getAsJsonObject("data");
        assertTrue(cartData.get("itemCount").getAsInt() > 0);

        // 6. POST /api/orders (Execute atomic checkout transaction)
        OrderApiServlet orderServlet = new OrderApiServlet();
        HttpServletRequest checkoutReq = mock(HttpServletRequest.class);
        when(checkoutReq.getRequestURI()).thenReturn("/api/orders");
        when(checkoutReq.getPathInfo()).thenReturn(null);
        when(checkoutReq.getSession(false)).thenReturn(session);

        ServletExecutionResult checkoutRes = new ServletExecutionResult();
        orderServlet.doPost(checkoutReq, checkoutRes.createResponseMock());

        assertEquals(201, checkoutRes.statusCode);
        JsonObject orderJson = checkoutRes.getJson();
        assertTrue(orderJson.get("success").getAsBoolean());
        JsonObject orderData = orderJson.getAsJsonObject("data");
        long newOrderId = orderData.get("id").getAsLong();
        assertTrue(newOrderId > 0);

        // 7. GET /api/orders/{id} (Check placed order details)
        HttpServletRequest orderDetailReq = mock(HttpServletRequest.class);
        when(orderDetailReq.getRequestURI()).thenReturn("/api/orders/" + newOrderId);
        when(orderDetailReq.getPathInfo()).thenReturn("/" + newOrderId);
        when(orderDetailReq.getSession(false)).thenReturn(session);

        ServletExecutionResult orderDetailRes = new ServletExecutionResult();
        orderServlet.doGet(orderDetailReq, orderDetailRes.createResponseMock());

        assertEquals(200, orderDetailRes.statusCode);
        assertTrue(orderDetailRes.getJson().get("success").getAsBoolean());

        // 8. GET /api/reviews?productId=1
        ReviewApiServlet reviewServlet = new ReviewApiServlet();
        HttpServletRequest getRevReq = mock(HttpServletRequest.class);
        when(getRevReq.getRequestURI()).thenReturn("/api/reviews");
        when(getRevReq.getParameter("productId")).thenReturn("1");

        ServletExecutionResult getRevRes = new ServletExecutionResult();
        reviewServlet.doGet(getRevReq, getRevRes.createResponseMock());

        assertEquals(200, getRevRes.statusCode);
        assertTrue(getRevRes.getJson().get("success").getAsBoolean());

        // 9. POST /api/chat (Test AI Assistant endpoint with server-side provider abstraction)
        ChatServlet chatServlet = new ChatServlet();
        HttpServletRequest chatReq = mock(HttpServletRequest.class);
        when(chatReq.getRequestURI()).thenReturn("/api/chat");
        when(chatReq.getContentType()).thenReturn("application/json");
        when(chatReq.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"message\":\"What headphones do you recommend?\"}"
        )));
        when(chatReq.getSession(true)).thenReturn(session);

        ServletExecutionResult chatRes = new ServletExecutionResult();
        chatServlet.doPost(chatReq, chatRes.createResponseMock());

        assertEquals(200, chatRes.statusCode);
        JsonObject chatJson = chatRes.getJson();
        assertTrue(chatJson.get("success").getAsBoolean());
        assertNotNull(chatJson.getAsJsonObject("data").get("reply").getAsString());
    }

    @Test
    @DisplayName("Verify Seller & Admin APIs: Listings -> Orders -> Stats")
    void testSellerAndAdminApis() throws Exception {
        // 1. Seller Login: techseller@rohithmart.com
        MockSessionState sellerSessionState = new MockSessionState();
        HttpSession sellerSession = sellerSessionState.createMock();

        AuthApiServlet authServlet = new AuthApiServlet();
        HttpServletRequest sellerLoginReq = mock(HttpServletRequest.class);
        when(sellerLoginReq.getRequestURI()).thenReturn("/api/auth/login");
        when(sellerLoginReq.getPathInfo()).thenReturn("/login");
        when(sellerLoginReq.getContentType()).thenReturn("application/json");
        when(sellerLoginReq.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"email\":\"techseller@rohithmart.com\",\"password\":\"Password@123\"}"
        )));
        when(sellerLoginReq.getSession(true)).thenReturn(sellerSession);

        ServletExecutionResult sellerLoginRes = new ServletExecutionResult();
        authServlet.doPost(sellerLoginReq, sellerLoginRes.createResponseMock());
        assertEquals(200, sellerLoginRes.statusCode);
        assertEquals("SELLER", sellerLoginRes.getJson().getAsJsonObject("data").get("role").getAsString());

        // 2. GET /api/seller/products
        SellerApiServlet sellerServlet = new SellerApiServlet();
        HttpServletRequest sellerProdReq = mock(HttpServletRequest.class);
        when(sellerProdReq.getRequestURI()).thenReturn("/api/seller/products");
        when(sellerProdReq.getPathInfo()).thenReturn("/products");
        when(sellerProdReq.getSession(false)).thenReturn(sellerSession);

        ServletExecutionResult sellerProdRes = new ServletExecutionResult();
        sellerServlet.doGet(sellerProdReq, sellerProdRes.createResponseMock());

        assertEquals(200, sellerProdRes.statusCode);
        assertTrue(sellerProdRes.getJson().get("success").getAsBoolean());
        JsonArray sellerProducts = sellerProdRes.getJson().getAsJsonArray("data");
        assertTrue(sellerProducts.size() > 0);

        // 3. Admin Login: admin@rohithmart.com
        MockSessionState adminSessionState = new MockSessionState();
        HttpSession adminSession = adminSessionState.createMock();

        HttpServletRequest adminLoginReq = mock(HttpServletRequest.class);
        when(adminLoginReq.getRequestURI()).thenReturn("/api/auth/login");
        when(adminLoginReq.getPathInfo()).thenReturn("/login");
        when(adminLoginReq.getContentType()).thenReturn("application/json");
        when(adminLoginReq.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"email\":\"admin@rohithmart.com\",\"password\":\"Password@123\"}"
        )));
        when(adminLoginReq.getSession(true)).thenReturn(adminSession);

        ServletExecutionResult adminLoginRes = new ServletExecutionResult();
        authServlet.doPost(adminLoginReq, adminLoginRes.createResponseMock());
        assertEquals(200, adminLoginRes.statusCode);
        assertEquals("ADMIN", adminLoginRes.getJson().getAsJsonObject("data").get("role").getAsString());

        // 4. GET /api/admin/stats
        AdminApiServlet adminServlet = new AdminApiServlet();
        HttpServletRequest adminStatsReq = mock(HttpServletRequest.class);
        when(adminStatsReq.getRequestURI()).thenReturn("/api/admin/stats");
        when(adminStatsReq.getPathInfo()).thenReturn("/stats");
        when(adminStatsReq.getSession(false)).thenReturn(adminSession);

        ServletExecutionResult adminStatsRes = new ServletExecutionResult();
        adminServlet.doGet(adminStatsReq, adminStatsRes.createResponseMock());

        assertEquals(200, adminStatsRes.statusCode);
        JsonObject statsJson = adminStatsRes.getJson();
        assertTrue(statsJson.get("success").getAsBoolean());
        JsonObject statsData = statsJson.getAsJsonObject("data");
        assertTrue(statsData.get("totalUsers").getAsInt() > 0);
        assertTrue(statsData.get("totalProducts").getAsInt() > 0);
        assertTrue(statsData.get("totalOrders").getAsInt() > 0);

        // 5. GET /api/admin/users
        HttpServletRequest adminUsersReq = mock(HttpServletRequest.class);
        when(adminUsersReq.getRequestURI()).thenReturn("/api/admin/users");
        when(adminUsersReq.getPathInfo()).thenReturn("/users");
        when(adminUsersReq.getSession(false)).thenReturn(adminSession);

        ServletExecutionResult adminUsersRes = new ServletExecutionResult();
        adminServlet.doGet(adminUsersReq, adminUsersRes.createResponseMock());

        assertEquals(200, adminUsersRes.statusCode);
        assertTrue(adminUsersRes.getJson().get("success").getAsBoolean());
        JsonArray usersList = adminUsersRes.getJson().getAsJsonArray("data");
        assertTrue(usersList.size() >= 5);
    }
}
