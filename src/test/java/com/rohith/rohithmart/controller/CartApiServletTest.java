package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.CartItemDTO;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.CartService;
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
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartApiServlet Tests")
class CartApiServletTest {

    @Mock
    private CartService cartService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private CartApiServlet servlet;
    private StringWriter responseWriter;
    private User testBuyer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CartApiServlet(cartService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setEmail("buyer@example.com");
        testBuyer.setRole(Role.BUYER);
    }

    @Test
    @DisplayName("GET /api/cart returns 200 with cart items and total")
    void testGetCartSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);

        CartItemDTO item = new CartItemDTO(101L, 10L, "Wireless Mouse", "mouse.png",
                new BigDecimal("29.99"), 2, new BigDecimal("59.98"), null);
        when(cartService.getCartItems(1L)).thenReturn(List.of(item));
        when(cartService.calculateTotal(any())).thenReturn(new BigDecimal("59.98"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
        JsonObject data = json.getAsJsonObject("data");
        assertEquals(1, data.get("itemCount").getAsInt());
    }

    @Test
    @DisplayName("POST /api/cart/add adds item and returns 201")
    void testAddToCartSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn("/add");
        when(request.getContentType()).thenReturn("application/json");

        String body = "{\"productId\":10,\"quantity\":2}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        servlet.doPost(request, response);

        verify(cartService).addToCart(1L, 10L, 2);
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("POST /api/cart/update updates quantity and returns 200")
    void testUpdateQuantitySuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn("/update");
        when(request.getContentType()).thenReturn("application/json");

        String body = "{\"cartItemId\":101,\"quantity\":5}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        servlet.doPost(request, response);

        verify(cartService).updateQuantity(1L, 101L, 5);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("POST /api/cart/remove removes item and returns 200")
    void testRemoveFromCartSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn("/remove");
        when(request.getContentType()).thenReturn("application/json");

        String body = "{\"cartItemId\":101}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        servlet.doPost(request, response);

        verify(cartService).removeFromCart(1L, 101L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("DELETE /api/cart clears cart and returns 200")
    void testClearCartSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn(null);

        servlet.doDelete(request, response);

        verify(cartService).clearCart(1L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
    }
}
