package com.rohith.rohithmart.util;

import com.rohith.rohithmart.dto.PaginationParams;
import com.rohith.rohithmart.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Comprehensive, reusable validation utility for incoming user and API inputs.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Password rule: min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#^~+=])[A-Za-z\\d@$!%*?&_\\-#^~+=]{8,128}$");

    private static final int MIN_PRODUCT_NAME_LENGTH = 2;
    private static final int MAX_PRODUCT_NAME_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final int MAX_SEARCH_QUERY_LENGTH = 100;
    private static final int MIN_CART_QUANTITY = 1;
    private static final int MAX_CART_QUANTITY = 99;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private ValidationUtil() {
    }

    // ==========================================
    // Required Fields & General String Checks
    // ==========================================

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean hasMinLength(String value, int minLength) {
        return value != null && value.trim().length() >= minLength;
    }

    public static String validateNotBlank(String value, String fieldName) throws ValidationException {
        if (!isNotEmpty(value)) {
            throw new ValidationException(fieldName + " is required", fieldName, fieldName + " cannot be blank");
        }
        return value.trim();
    }

    public static <T> T validateNotNull(T value, String fieldName) throws ValidationException {
        if (value == null) {
            throw new ValidationException(fieldName + " is required", fieldName, fieldName + " cannot be null");
        }
        return value;
    }

    public static String validateRequired(String value, String fieldName) throws ValidationException {
        return validateNotBlank(value, fieldName);
    }

    // ==========================================
    // Email Validation
    // ==========================================

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static String validateEmail(String email) throws ValidationException {
        String trimmed = validateNotBlank(email, "email");
        if (!isValidEmail(trimmed)) {
            throw new ValidationException("Invalid email format: " + trimmed, "email", "Please provide a valid email address");
        }
        return trimmed.toLowerCase();
    }

    // ==========================================
    // Password Rules Validation
    // ==========================================

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static String validatePassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required", "password", "Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters", "password", "Password must be at least 8 characters long");
        }
        if (!isValidPassword(password)) {
            throw new ValidationException("Password does not meet complexity requirements", "password",
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&_-#^~+=)");
        }
        return password;
    }

    // ==========================================
    // Product Name & Description Validation
    // ==========================================

    public static boolean isValidProductName(String name) {
        if (!isNotEmpty(name)) return false;
        int len = name.trim().length();
        return len >= MIN_PRODUCT_NAME_LENGTH && len <= MAX_PRODUCT_NAME_LENGTH;
    }

    public static String validateProductName(String name) throws ValidationException {
        String trimmed = validateNotBlank(name, "name");
        if (trimmed.length() < MIN_PRODUCT_NAME_LENGTH || trimmed.length() > MAX_PRODUCT_NAME_LENGTH) {
            throw new ValidationException("Product name must be between " + MIN_PRODUCT_NAME_LENGTH +
                    " and " + MAX_PRODUCT_NAME_LENGTH + " characters", "name",
                    "Product name must be between " + MIN_PRODUCT_NAME_LENGTH + " and " + MAX_PRODUCT_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    public static boolean isValidDescription(String description) {
        if (description == null) return true;
        return description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    public static String validateDescription(String description) throws ValidationException {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Description exceeds maximum length of " + MAX_DESCRIPTION_LENGTH + " characters",
                    "description", "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return description != null ? description.trim() : "";
    }

    // ==========================================
    // Price Validation
    // ==========================================

    public static boolean isValidPrice(BigDecimal price) {
        if (price == null) return false;
        if (price.compareTo(BigDecimal.ZERO) <= 0) return false;
        // Check max 2 decimal places
        BigDecimal normalized = price.stripTrailingZeros();
        return normalized.scale() <= 2;
    }

    public static BigDecimal validatePrice(BigDecimal price) throws ValidationException {
        validateNotNull(price, "price");
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero", "price", "Price must be strictly positive");
        }
        BigDecimal normalized = price.stripTrailingZeros();
        if (normalized.scale() > 2) {
            throw new ValidationException("Price cannot have more than 2 decimal places", "price", "Price can have at most 2 decimal places");
        }
        return price;
    }

    public static BigDecimal validatePrice(String priceStr) throws ValidationException {
        String trimmed = validateNotBlank(priceStr, "price");
        try {
            BigDecimal price = new BigDecimal(trimmed);
            return validatePrice(price);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid price format: " + priceStr, "price", "Price must be a valid number");
        }
    }

    // ==========================================
    // Stock Quantity Validation
    // ==========================================

    public static boolean isValidStockQuantity(Integer stock) {
        return stock != null && stock >= 0;
    }

    public static Integer validateStockQuantity(Integer stock) throws ValidationException {
        validateNotNull(stock, "stock");
        if (stock < 0) {
            throw new ValidationException("Stock quantity cannot be negative", "stock", "Stock must be 0 or greater");
        }
        return stock;
    }

    public static Integer validateStockQuantity(String stockStr) throws ValidationException {
        String trimmed = validateNotBlank(stockStr, "stock");
        try {
            int stock = Integer.parseInt(trimmed);
            return validateStockQuantity(stock);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid stock format: " + stockStr, "stock", "Stock must be an integer");
        }
    }

    // ==========================================
    // Cart Quantity Validation (1 to 99)
    // ==========================================

    public static boolean isValidCartQuantity(Integer quantity) {
        return quantity != null && quantity >= MIN_CART_QUANTITY && quantity <= MAX_CART_QUANTITY;
    }

    public static Integer validateCartQuantity(Integer quantity) throws ValidationException {
        validateNotNull(quantity, "quantity");
        if (quantity < MIN_CART_QUANTITY || quantity > MAX_CART_QUANTITY) {
            throw new ValidationException("Cart quantity must be between " + MIN_CART_QUANTITY + " and " + MAX_CART_QUANTITY,
                    "quantity", "Quantity must be between " + MIN_CART_QUANTITY + " and " + MAX_CART_QUANTITY);
        }
        return quantity;
    }

    public static Integer validateCartQuantity(String quantityStr) throws ValidationException {
        String trimmed = validateNotBlank(quantityStr, "quantity");
        try {
            int quantity = Integer.parseInt(trimmed);
            return validateCartQuantity(quantity);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid quantity format: " + quantityStr, "quantity", "Quantity must be an integer");
        }
    }

    // ==========================================
    // Rating Validation (1 to 5)
    // ==========================================

    public static boolean isValidRating(Integer rating) {
        return rating != null && rating >= MIN_RATING && rating <= MAX_RATING;
    }

    public static Integer validateRating(Integer rating) throws ValidationException {
        validateNotNull(rating, "rating");
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new ValidationException("Rating must be between " + MIN_RATING + " and " + MAX_RATING,
                    "rating", "Rating must be an integer between " + MIN_RATING + " and " + MAX_RATING);
        }
        return rating;
    }

    public static Integer validateRating(String ratingStr) throws ValidationException {
        String trimmed = validateNotBlank(ratingStr, "rating");
        try {
            int rating = Integer.parseInt(trimmed);
            return validateRating(rating);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid rating format: " + ratingStr, "rating", "Rating must be an integer between 1 and 5");
        }
    }

    // ==========================================
    // Review Comments Validation
    // ==========================================

    public static boolean isValidComment(String comment) {
        if (comment == null) return true;
        return comment.length() <= MAX_COMMENT_LENGTH;
    }

    public static String validateComment(String comment) throws ValidationException {
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new ValidationException("Comment exceeds maximum length of " + MAX_COMMENT_LENGTH + " characters",
                    "comment", "Comment cannot exceed " + MAX_COMMENT_LENGTH + " characters");
        }
        return comment != null ? comment.trim() : "";
    }

    // ==========================================
    // ID Validation (Positive Long > 0)
    // ==========================================

    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    public static Long validateId(Long id, String fieldName) throws ValidationException {
        validateNotNull(id, fieldName);
        if (id <= 0) {
            throw new ValidationException(fieldName + " must be a positive number", fieldName, fieldName + " must be greater than 0");
        }
        return id;
    }

    public static Long validateId(String idStr, String fieldName) throws ValidationException {
        String trimmed = validateNotBlank(idStr, fieldName);
        try {
            long id = Long.parseLong(trimmed);
            return validateId(id, fieldName);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid ID format for " + fieldName + ": " + idStr, fieldName, fieldName + " must be a valid numeric ID");
        }
    }

    // ==========================================
    // Pagination Validation
    // ==========================================

    public static PaginationParams validatePagination(Integer page, Integer size) {
        int p = (page != null && page > 0) ? page : PaginationParams.DEFAULT_PAGE;
        int s = (size != null && size > 0) ? Math.min(size, PaginationParams.MAX_PAGE_SIZE) : PaginationParams.DEFAULT_PAGE_SIZE;
        return new PaginationParams(p, s);
    }

    public static PaginationParams validatePagination(String pageStr, String sizeStr) {
        int page = PaginationParams.DEFAULT_PAGE;
        int size = PaginationParams.DEFAULT_PAGE_SIZE;

        if (isNotEmpty(pageStr)) {
            try {
                int p = Integer.parseInt(pageStr.trim());
                if (p > 0) page = p;
            } catch (NumberFormatException ignored) {
            }
        }
        if (isNotEmpty(sizeStr)) {
            try {
                int s = Integer.parseInt(sizeStr.trim());
                if (s > 0) size = Math.min(s, PaginationParams.MAX_PAGE_SIZE);
            } catch (NumberFormatException ignored) {
            }
        }
        return new PaginationParams(page, size);
    }

    // ==========================================
    // Search Parameters Validation
    // ==========================================

    public static boolean isValidSearchQuery(String query) {
        if (query == null) return true;
        return query.trim().length() <= MAX_SEARCH_QUERY_LENGTH;
    }

    public static String validateSearchQuery(String query) throws ValidationException {
        if (query == null) return "";
        String trimmed = query.trim();
        if (trimmed.length() > MAX_SEARCH_QUERY_LENGTH) {
            throw new ValidationException("Search query exceeds maximum length of " + MAX_SEARCH_QUERY_LENGTH + " characters",
                    "search", "Search query cannot exceed " + MAX_SEARCH_QUERY_LENGTH + " characters");
        }
        // Sanitize control characters
        return trimmed.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }
}
