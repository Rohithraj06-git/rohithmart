package com.rohith.rohithmart.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exception Hierarchy and HTTP Status Mapping Unit Tests")
class ExceptionHierarchyTest {

    @Test
    @DisplayName("ValidationException: HTTP 400, VALIDATION_ERROR, field-level error mapping")
    void testValidationException() {
        ValidationException ex = new ValidationException("Invalid email format", "email", "Email must be valid");
        assertEquals(400, ex.getStatusCode());
        assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        assertEquals(1, ex.getFieldErrors().size());
        assertEquals("Email must be valid", ex.getFieldErrors().get("email"));

        ValidationException mapEx = new ValidationException("Multiple failures",
                Map.of("name", "Required", "price", "Must be positive"));
        assertEquals(400, mapEx.getStatusCode());
        assertEquals(2, mapEx.getFieldErrors().size());
    }

    @Test
    @DisplayName("AuthenticationException: HTTP 401, UNAUTHENTICATED")
    void testAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Invalid email or password");
        assertEquals(401, ex.getStatusCode());
        assertEquals("UNAUTHENTICATED", ex.getErrorCode());
        assertTrue(ex instanceof AppException);
    }

    @Test
    @DisplayName("AuthorizationException & ForbiddenException: HTTP 403, ACCESS_DENIED / FORBIDDEN")
    void testAuthorizationException() {
        AuthorizationException authz = new AuthorizationException("Only sellers may create products");
        assertEquals(403, authz.getStatusCode());
        assertEquals("ACCESS_DENIED", authz.getErrorCode());

        ForbiddenException forbidden = new ForbiddenException("You cannot modify another user's cart");
        assertEquals(403, forbidden.getStatusCode());
        assertEquals("FORBIDDEN", forbidden.getErrorCode());
        assertTrue(forbidden instanceof AuthorizationException);
    }

    @Test
    @DisplayName("ResourceNotFoundException & ProductNotFoundException: HTTP 404, RESOURCE_NOT_FOUND / PRODUCT_NOT_FOUND")
    void testResourceNotFoundExceptions() {
        ResourceNotFoundException rnf = new ResourceNotFoundException("User not found with id: 42");
        assertEquals(404, rnf.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", rnf.getErrorCode());

        ProductNotFoundException pnf = new ProductNotFoundException(999L);
        assertEquals(404, pnf.getStatusCode());
        assertEquals("PRODUCT_NOT_FOUND", pnf.getErrorCode());
        assertEquals(999L, pnf.getProductId());
        assertTrue(pnf.getMessage().contains("999"));
        assertTrue(pnf instanceof ResourceNotFoundException);
    }

    @Test
    @DisplayName("InsufficientStockException: HTTP 400, INSUFFICIENT_STOCK")
    void testInsufficientStockException() {
        InsufficientStockException ex = new InsufficientStockException(10L, 5, 2);
        assertEquals(400, ex.getStatusCode());
        assertEquals("INSUFFICIENT_STOCK", ex.getErrorCode());
        assertEquals(10L, ex.getProductId());
        assertEquals(5, ex.getRequestedQuantity());
        assertEquals(2, ex.getAvailableStock());
        assertTrue(ex.getMessage().contains("10"));
    }

    @Test
    @DisplayName("OrderException: HTTP 400, ORDER_ERROR")
    void testOrderException() {
        OrderException ex = new OrderException("Cannot cancel an order that has already been delivered");
        assertEquals(400, ex.getStatusCode());
        assertEquals("ORDER_ERROR", ex.getErrorCode());
        assertTrue(ex instanceof AppException);
    }

    @Test
    @DisplayName("DatabaseException: HTTP 500, DATABASE_ERROR with cause")
    void testDatabaseException() {
        Throwable cause = new RuntimeException("Deadlock detected");
        DatabaseException ex = new DatabaseException("Failed to execute SQL query", cause);
        assertEquals(500, ex.getStatusCode());
        assertEquals("DATABASE_ERROR", ex.getErrorCode());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("ConflictException: HTTP 409, CONFLICT")
    void testConflictException() {
        ConflictException ex = new ConflictException("User already exists with email: test@example.com");
        assertEquals(409, ex.getStatusCode());
        assertEquals("CONFLICT", ex.getErrorCode());
        assertTrue(ex instanceof AppException);
    }
}
