package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.model.OrderStatus;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.UserService;
import com.rohith.rohithmart.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Seller and Admin API Servlet Tests")
class SellerAndAdminApiServletTest {

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private SellerApiServlet sellerServlet;
    private AdminApiServlet adminServlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        sellerServlet = new SellerApiServlet(productService, orderService);
        adminServlet = new AdminApiServlet(userService, productService, orderService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("SellerApi: GET /api/seller/products returns seller products")
    void testSellerGetProducts() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User seller = new User();
        seller.setId(2L);
        seller.setRole(Role.SELLER);
        when(session.getAttribute("user")).thenReturn(seller);
        when(request.getPathInfo()).thenReturn("/products");

        ProductDTO product = ProductDTO.builder().id(10L).name("Gadget").sellerId(2L).build();
        when(productService.getProductsBySeller(2L)).thenReturn(List.of(product));

        sellerServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("SellerApi: POST /api/seller/orders/5/status updates status")
    void testSellerUpdateOrderStatus() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User seller = new User();
        seller.setId(2L);
        seller.setRole(Role.SELLER);
        when(session.getAttribute("user")).thenReturn(seller);
        when(request.getPathInfo()).thenReturn("/orders/5/status");
        when(request.getParameter("status")).thenReturn("SHIPPED");

        sellerServlet.doPost(request, response);

        verify(orderService).updateOrderStatus(5L, OrderStatus.SHIPPED, 2L, false);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("AdminApi: GET /api/admin/stats returns statistics")
    void testAdminGetStats() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User admin = new User();
        admin.setId(99L);
        admin.setRole(Role.ADMIN);
        when(session.getAttribute("user")).thenReturn(admin);
        when(request.getPathInfo()).thenReturn("/stats");

        when(userService.getTotalUserCount()).thenReturn(50);
        when(productService.getTotalProductCount()).thenReturn(200);
        when(orderService.getTotalOrderCount()).thenReturn(150);

        adminServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
        JsonObject data = obj.getAsJsonObject("data");
        assertEquals(50, data.get("totalUsers").getAsInt());
        assertEquals(200, data.get("totalProducts").getAsInt());
        assertEquals(150, data.get("totalOrders").getAsInt());
    }

    @Test
    @DisplayName("AdminApi: POST /api/admin/users/3/role updates user role")
    void testAdminUpdateUserRole() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User admin = new User();
        admin.setId(99L);
        admin.setRole(Role.ADMIN);
        when(session.getAttribute("user")).thenReturn(admin);
        when(request.getPathInfo()).thenReturn("/users/3/role");
        when(request.getParameter("role")).thenReturn("SELLER");

        UserResponseDTO updated = new UserResponseDTO(3L, "User Three", "user3@example.com", Role.SELLER, Timestamp.from(Instant.now()));
        when(userService.updateUserRole(99L, 3L, Role.SELLER)).thenReturn(updated);

        adminServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
    }
}
