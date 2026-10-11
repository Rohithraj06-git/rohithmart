package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.PagedResponse;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.dto.ProductFilter;
import com.rohith.rohithmart.exception.ProductNotFoundException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.ProductService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("ProductApiServlet Tests")
class ProductApiServletTest {

    @Mock
    private ProductService productService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ProductApiServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProductApiServlet(productService);
        responseWriter = new StringWriter();
        lenient().when(request.getRequestURI()).thenReturn("/api/products");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("GET /api/products returns 200 with paged response")
    void testGetProductsPaged() throws Exception {
        when(request.getPathInfo()).thenReturn(null);
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("size")).thenReturn("20");
        when(request.getParameter("category")).thenReturn("Electronics");
        when(request.getParameter("sort")).thenReturn("price_asc");

        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .build();
        PagedResponse<ProductDTO> paged = PagedResponse.of(List.of(product), 1, 20, 1L);

        when(productService.searchProductsWithFilters(any(ProductFilter.class))).thenReturn(paged);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        JsonObject obj = JsonUtil.fromJson(json, JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
        assertNotNull(obj.get("data"));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 200 with product details")
    void testGetProductById() throws Exception {
        when(request.getPathInfo()).thenReturn("/10");
        ProductDTO product = ProductDTO.builder()
                .id(10L)
                .name("Smartphone")
                .price(new BigDecimal("499.99"))
                .build();
        when(productService.getProductById(10L)).thenReturn(product);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        JsonObject obj = JsonUtil.fromJson(json, JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
        assertEquals(10L, obj.getAsJsonObject("data").get("id").getAsLong());
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 404 when product does not exist")
    void testGetProductByIdNotFound() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/products/999");
        when(request.getPathInfo()).thenReturn("/999");
        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException(999L));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        String json = responseWriter.toString();
        JsonObject obj = JsonUtil.fromJson(json, JsonObject.class);
        assertFalse(obj.get("success").getAsBoolean());
        assertEquals("PRODUCT_NOT_FOUND", obj.get("errorCode").getAsString());
    }

    @Test
    @DisplayName("POST /api/products returns 401 when unauthenticated")
    void testCreateProductUnauthenticated() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = responseWriter.toString();
        JsonObject obj = JsonUtil.fromJson(json, JsonObject.class);
        assertFalse(obj.get("success").getAsBoolean());
        assertEquals("UNAUTHENTICATED", obj.get("errorCode").getAsString());
    }

    @Test
    @DisplayName("POST /api/products returns 403 when buyer tries to create product")
    void testCreateProductAsBuyerForbidden() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getSession(false)).thenReturn(session);
        User buyer = new User();
        buyer.setId(1L);
        buyer.setRole(Role.BUYER);
        when(session.getAttribute("user")).thenReturn(buyer);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        String json = responseWriter.toString();
        JsonObject obj = JsonUtil.fromJson(json, JsonObject.class);
        assertFalse(obj.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("POST /api/products returns 201 when seller creates product")
    void testCreateProductAsSellerSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User seller = new User();
        seller.setId(2L);
        seller.setRole(Role.SELLER);
        when(session.getAttribute("user")).thenReturn(seller);

        String jsonInput = "{\"name\":\"Mechanical Keyboard\",\"price\":79.99,\"stockQty\":20,\"category\":\"Electronics\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonInput)));

        ProductDTO created = ProductDTO.builder()
                .id(15L)
                .sellerId(2L)
                .name("Mechanical Keyboard")
                .price(new BigDecimal("79.99"))
                .stockQty(20)
                .category("Electronics")
                .build();
        when(productService.createProduct(any(ProductDTO.class), eq(2L))).thenReturn(created);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        String json = responseWriter.toString();
        JsonObject obj = JsonUtil.fromJson(json, JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
        assertEquals(15L, obj.getAsJsonObject("data").get("id").getAsLong());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} returns 200 when seller deletes product")
    void testDeleteProductSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        User seller = new User();
        seller.setId(2L);
        seller.setRole(Role.SELLER);
        when(session.getAttribute("user")).thenReturn(seller);
        when(request.getPathInfo()).thenReturn("/15");

        servlet.doDelete(request, response);

        verify(productService).deleteProduct(15L, 2L, false);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
    }
}
