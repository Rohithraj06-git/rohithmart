package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.model.OrderStatus;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
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
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderApiServlet Tests")
class OrderApiServletTest {

    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private OrderApiServlet servlet;
    private StringWriter responseWriter;
    private User testBuyer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new OrderApiServlet(orderService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setRole(Role.BUYER);
    }

    @Test
    @DisplayName("GET /api/orders returns 200 with list of buyer orders")
    void testGetBuyerOrdersSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn(null);

        OrderDTO order = new OrderDTO(50L, 1L, OrderStatus.PENDING, new BigDecimal("120.00"), null, null);
        when(orderService.getBuyerOrders(1L)).thenReturn(List.of(order));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
        assertEquals(1, json.getAsJsonArray("data").size());
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 200 with order details")
    void testGetOrderByIdSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn("/50");

        OrderDTO order = new OrderDTO(50L, 1L, OrderStatus.CONFIRMED, new BigDecimal("120.00"), null, null);
        when(orderService.getOrderById(50L, 1L, false)).thenReturn(order);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
        assertEquals(50L, json.getAsJsonObject("data").get("id").getAsLong());
    }

    @Test
    @DisplayName("POST /api/orders executes checkout and returns 201")
    void testCheckoutSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn(null);

        OrderDTO order = new OrderDTO(55L, 1L, OrderStatus.PENDING, new BigDecimal("250.00"), null, null);
        when(orderService.checkout(1L)).thenReturn(order);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
        assertEquals(55L, json.getAsJsonObject("data").get("id").getAsLong());
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel cancels order and returns 200")
    void testCancelOrderSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getPathInfo()).thenReturn("/50/cancel");

        servlet.doPost(request, response);

        verify(orderService).cancelOrder(50L, 1L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
    }
}
