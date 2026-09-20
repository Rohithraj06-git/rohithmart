package com.rohith.rohithmart.util;

import com.rohith.rohithmart.dto.PaginationParams;
import com.rohith.rohithmart.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtil Comprehensive Unit Tests")
class ValidationUtilTest {

    @Nested
    @DisplayName("1. Required Fields Validation")
    class RequiredFieldsTests {
        @Test
        void testValidateNotBlankSuccess() throws ValidationException {
            assertEquals("valid", ValidationUtil.validateNotBlank("  valid  ", "name"));
        }

        @Test
        void testValidateNotBlankFailures() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validateNotBlank(null, "name"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateNotBlank("", "name"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateNotBlank("   ", "name"));
        }

        @Test
        void testValidateNotNull() throws ValidationException {
            String val = "hello";
            assertSame(val, ValidationUtil.validateNotNull(val, "item"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateNotNull(null, "item"));
        }
    }

    @Nested
    @DisplayName("2. Email Format Validation")
    class EmailValidationTests {
        @Test
        void testValidEmails() throws ValidationException {
            assertEquals("user@example.com", ValidationUtil.validateEmail("user@example.com"));
            assertEquals("john.doe+tag@sub.domain.co", ValidationUtil.validateEmail("john.doe+tag@sub.domain.co"));
        }

        @Test
        void testInvalidEmails() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("plainaddress"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("@missinguser.com"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("user@.com"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("user@domain"));
            assertFalse(ValidationUtil.isValidEmail(null));
        }
    }

    @Nested
    @DisplayName("3. Password Rules Validation")
    class PasswordValidationTests {
        @Test
        void testValidPassword() throws ValidationException {
            assertEquals("Password@123", ValidationUtil.validatePassword("Password@123"));
            assertEquals("Strong#Pass99", ValidationUtil.validatePassword("Strong#Pass99"));
        }

        @Test
        void testInvalidPasswords() {
            // Less than 8 chars
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("Pass@1"));
            // Missing uppercase
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("password@123"));
            // Missing lowercase
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("PASSWORD@123"));
            // Missing digit
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("Password@abc"));
            // Missing special char
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("Password1234"));
            // Null or empty
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword(null));
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword(""));
        }
    }

    @Nested
    @DisplayName("4. Product Name Validation")
    class ProductNameValidationTests {
        @Test
        void testValidProductName() throws ValidationException {
            assertEquals("Mechanical Keyboard", ValidationUtil.validateProductName("  Mechanical Keyboard  "));
        }

        @Test
        void testInvalidProductName() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validateProductName("A")); // too short (< 2)
            assertThrows(ValidationException.class, () -> ValidationUtil.validateProductName("   "));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateProductName("A".repeat(201))); // too long (> 200)
        }
    }

    @Nested
    @DisplayName("5. Description Validation")
    class DescriptionValidationTests {
        @Test
        void testValidDescription() throws ValidationException {
            assertEquals("", ValidationUtil.validateDescription(null));
            assertEquals("Detailed description", ValidationUtil.validateDescription("  Detailed description  "));
        }

        @Test
        void testDescriptionTooLong() {
            String excessive = "A".repeat(5001);
            assertThrows(ValidationException.class, () -> ValidationUtil.validateDescription(excessive));
        }
    }

    @Nested
    @DisplayName("6. Price Validation")
    class PriceValidationTests {
        @Test
        void testValidPrice() throws ValidationException {
            assertEquals(new BigDecimal("19.99"), ValidationUtil.validatePrice(new BigDecimal("19.99")));
            assertEquals(new BigDecimal("100"), ValidationUtil.validatePrice(new BigDecimal("100")));
            assertEquals(new BigDecimal("49.50"), ValidationUtil.validatePrice("49.50"));
        }

        @Test
        void testInvalidPrice() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePrice(BigDecimal.ZERO));
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePrice(new BigDecimal("-5.00")));
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePrice(new BigDecimal("19.999"))); // 3 decimals
            assertThrows(ValidationException.class, () -> ValidationUtil.validatePrice("invalid-number"));
        }
    }

    @Nested
    @DisplayName("7. Stock Quantity Validation")
    class StockQuantityValidationTests {
        @Test
        void testValidStock() throws ValidationException {
            assertEquals(0, ValidationUtil.validateStockQuantity(0));
            assertEquals(500, ValidationUtil.validateStockQuantity(500));
            assertEquals(25, ValidationUtil.validateStockQuantity("25"));
        }

        @Test
        void testInvalidStock() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validateStockQuantity(-1));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateStockQuantity((Integer) null));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateStockQuantity("abc"));
        }
    }

    @Nested
    @DisplayName("8. Cart Quantity Validation (1-99)")
    class CartQuantityValidationTests {
        @Test
        void testValidCartQuantity() throws ValidationException {
            assertEquals(1, ValidationUtil.validateCartQuantity(1));
            assertEquals(99, ValidationUtil.validateCartQuantity(99));
            assertEquals(5, ValidationUtil.validateCartQuantity("5"));
        }

        @Test
        void testInvalidCartQuantity() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validateCartQuantity(0));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateCartQuantity(100));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateCartQuantity(-5));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateCartQuantity("xyz"));
        }
    }

    @Nested
    @DisplayName("9. Rating Validation (1-5)")
    class RatingValidationTests {
        @Test
        void testValidRating() throws ValidationException {
            assertEquals(1, ValidationUtil.validateRating(1));
            assertEquals(5, ValidationUtil.validateRating(5));
            assertEquals(4, ValidationUtil.validateRating("4"));
        }

        @Test
        void testInvalidRating() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(0));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(6));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(-1));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateRating("five"));
        }
    }

    @Nested
    @DisplayName("10. Review Comments Validation")
    class CommentValidationTests {
        @Test
        void testValidComment() throws ValidationException {
            assertEquals("", ValidationUtil.validateComment(null));
            assertEquals("Great product!", ValidationUtil.validateComment("  Great product!  "));
        }

        @Test
        void testCommentTooLong() {
            String excessive = "C".repeat(1001);
            assertThrows(ValidationException.class, () -> ValidationUtil.validateComment(excessive));
        }
    }

    @Nested
    @DisplayName("11. IDs Validation")
    class IdValidationTests {
        @Test
        void testValidId() throws ValidationException {
            assertEquals(1L, ValidationUtil.validateId(1L, "userId"));
            assertEquals(9999L, ValidationUtil.validateId("9999", "orderId"));
        }

        @Test
        void testInvalidId() {
            assertThrows(ValidationException.class, () -> ValidationUtil.validateId(0L, "id"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateId(-10L, "id"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateId((Long) null, "id"));
            assertThrows(ValidationException.class, () -> ValidationUtil.validateId("not-a-number", "id"));
        }
    }

    @Nested
    @DisplayName("12. Pagination Parameters Validation")
    class PaginationValidationTests {
        @Test
        void testValidPagination() {
            PaginationParams p1 = ValidationUtil.validatePagination(2, 20);
            assertEquals(2, p1.getPage());
            assertEquals(20, p1.getPageSize());
            assertEquals(20, p1.getOffset()); // (2 - 1) * 20

            PaginationParams p2 = ValidationUtil.validatePagination("3", "15");
            assertEquals(3, p2.getPage());
            assertEquals(15, p2.getPageSize());
            assertEquals(30, p2.getOffset());
        }

        @Test
        void testPaginationDefaultsAndSanitization() {
            // Negative or zero defaults to page 1, size 10
            PaginationParams p = ValidationUtil.validatePagination(-1, 0);
            assertEquals(1, p.getPage());
            assertEquals(10, p.getPageSize());
            assertEquals(0, p.getOffset());

            // Max page size capped at 100
            PaginationParams capped = ValidationUtil.validatePagination(1, 500);
            assertEquals(100, capped.getPageSize());

            // String non-numeric defaults
            PaginationParams invalidStr = ValidationUtil.validatePagination("abc", "xyz");
            assertEquals(1, invalidStr.getPage());
            assertEquals(10, invalidStr.getPageSize());
        }
    }

    @Nested
    @DisplayName("13. Search Parameters Validation")
    class SearchQueryValidationTests {
        @Test
        void testValidSearchQuery() throws ValidationException {
            assertEquals("wireless mouse", ValidationUtil.validateSearchQuery("  wireless mouse  "));
            assertEquals("", ValidationUtil.validateSearchQuery(null));
        }

        @Test
        void testSearchQueryTooLong() {
            String longQuery = "Q".repeat(101);
            assertThrows(ValidationException.class, () -> ValidationUtil.validateSearchQuery(longQuery));
        }
    }

    @Nested
    @DisplayName("14. ValidationResult Multi-Error Accumulator")
    class ValidationResultTests {
        @Test
        void testValidationResultSuccess() {
            ValidationResult result = new ValidationResult();
            result.checkNotBlank("name", "Product 1", "Name required");
            result.check(10 > 0, "quantity", "Quantity must be positive");
            assertTrue(result.isValid());
            assertFalse(result.hasErrors());
            assertDoesNotThrow(() -> result.throwIfInvalid());
        }

        @Test
        void testValidationResultAccumulatesErrors() {
            ValidationResult result = new ValidationResult();
            result.checkNotBlank("name", "", "Name is required");
            result.checkNotNull("category", null, "Category cannot be null");
            result.check(false, "price", "Price must be positive");

            assertTrue(result.hasErrors());
            assertEquals(3, result.getErrors().size());
            assertEquals("Name is required", result.getErrors().get("name"));
            assertEquals("Category cannot be null", result.getErrors().get("category"));
            assertEquals("Price must be positive", result.getErrors().get("price"));

            ValidationException ex = assertThrows(ValidationException.class, result::throwIfInvalid);
            assertEquals(400, ex.getStatusCode());
            assertEquals("VALIDATION_ERROR", ex.getErrorCode());
            assertEquals(3, ex.getFieldErrors().size());
        }
    }
}
