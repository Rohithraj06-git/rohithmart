package com.rohith.rohithmart.dto;

import com.google.gson.Gson;
import com.rohith.rohithmart.model.OrderStatus;
import com.rohith.rohithmart.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Security & Serialization Tests")
class DtoSecurityTest {

    private final Gson gson = new Gson();

    @Test
    @DisplayName("Security: UserResponse must never expose password or password_hash fields")
    void testUserResponseHasNoPasswordFields() {
        Class<?> clazz = UserResponse.class;
        List<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        assertFalse(fieldNames.contains("password"), "UserResponse must not have a 'password' field");
        assertFalse(fieldNames.contains("passwordhash"), "UserResponse must not have a 'passwordHash' field");
        assertFalse(fieldNames.contains("hash"), "UserResponse must not have a 'hash' field");

        // Check methods as well
        List<String> methodNames = Arrays.stream(clazz.getDeclaredMethods())
                .map(java.lang.reflect.Method::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        assertFalse(methodNames.contains("getpassword"), "UserResponse must not have 'getPassword'");
        assertFalse(methodNames.contains("getpasswordhash"), "UserResponse must not have 'getPasswordHash'");
    }

    @Test
    @DisplayName("Security: LoginRequest and RegisterRequest must mask sensitive password in toString()")
    void testRequestDtoToStringMasking() {
        String secret = "SuperSecret#2026";

        LoginRequest login = new LoginRequest("alice@example.com", secret);
        assertFalse(login.toString().contains(secret), "LoginRequest.toString() must not expose raw password");
        assertTrue(login.toString().contains("[MASKED]"), "LoginRequest.toString() must mask password");

        RegisterRequest register = new RegisterRequest("Alice", "alice@example.com", secret, Role.BUYER);
        assertFalse(register.toString().contains(secret), "RegisterRequest.toString() must not expose raw password");
        assertTrue(register.toString().contains("[MASKED]"), "RegisterRequest.toString() must mask password");
    }

    @Test
    @DisplayName("Builder pattern: UserResponse, ProductResponse, OrderResponse, ReviewResponse")
    void testBuilders() {
        Timestamp now = Timestamp.from(Instant.now());

        // UserResponse Builder
        UserResponse user = UserResponse.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .role(Role.BUYER)
                .createdAt(now)
                .build();
        assertEquals(1L, user.getId());
        assertEquals("alice@example.com", user.getEmail());

        // ProductResponse Builder
        ProductResponse prod = ProductResponse.builder()
                .id(10L)
                .sellerId(2L)
                .name("Smartphone")
                .description("Flagship")
                .price(new BigDecimal("799.99"))
                .stockQty(25)
                .category("Electronics")
                .imageUrl("phone.png")
                .createdAt(now)
                .build();
        assertEquals(10L, prod.getId());
        assertEquals("Smartphone", prod.getName());

        // OrderResponse & OrderItemResponse Builder
        OrderItemResponse item = new OrderItemResponse(1L, 100L, 10L, "Smartphone", 1, new BigDecimal("799.99"), new BigDecimal("799.99"), now);
        OrderResponse order = OrderResponse.builder()
                .id(100L)
                .buyerId(1L)
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("799.99"))
                .createdAt(now)
                .addItem(item)
                .build();
        assertEquals(100L, order.getId());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(1, order.getItems().size());

        // ReviewResponse Builder
        ReviewResponse review = ReviewResponse.builder()
                .id(50L)
                .productId(10L)
                .userId(1L)
                .userName("Alice")
                .rating(5)
                .comment("Excellent quality!")
                .createdAt(now)
                .build();
        assertEquals(50L, review.getId());
        assertEquals(5, review.getRating());
    }

    @Test
    @DisplayName("API Response envelope serialization: success and error structures")
    void testApiResponseEnvelopeSerialization() {
        // Success Envelope
        ApiResponse<Map<String, Object>> successResp = ApiResponse.success("Operation completed", Map.of("key", "value123"));
        String successJson = gson.toJson(successResp);
        assertTrue(successJson.contains("\"success\":true"), "JSON must contain 'success': true");
        assertTrue(successJson.contains("\"message\":\"Operation completed\""), "JSON must contain expected message");
        assertTrue(successJson.contains("\"data\":{\"key\":\"value123\"}"), "JSON must contain data payload");

        // Error Envelope with errorCode
        ApiResponse<Void> errResp = ApiResponse.error("RESOURCE_NOT_FOUND", "Resource not found");
        String errorJson = gson.toJson(errResp);
        assertTrue(errorJson.contains("\"success\":false"), "JSON must contain 'success': false");
        assertTrue(errorJson.contains("\"message\":\"Resource not found\""), "JSON must contain error message");
        assertTrue(errorJson.contains("\"errorCode\":\"RESOURCE_NOT_FOUND\""), "JSON must contain error code");

        // Error Envelope with fieldErrors
        ApiResponse<Void> validationErrResp = ApiResponse.error("VALIDATION_ERROR", "Validation failed",
                Map.of("email", "Invalid email format"));
        String valJson = gson.toJson(validationErrResp);
        assertTrue(valJson.contains("\"success\":false"));
        assertTrue(valJson.contains("\"errorCode\":\"VALIDATION_ERROR\""));
        assertTrue(valJson.contains("\"fieldErrors\":{\"email\":\"Invalid email format\"}"));
    }
}
