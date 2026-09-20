package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dto.PagedResponse;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.dto.ProductFilter;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests (Mockito)")
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productDAO);
    }

    @Test
    @DisplayName("createProduct: success with valid data")
    void testCreateProductSuccess() throws Exception {
        ProductDTO input = ProductDTO.builder()
                .name("Wireless Headset")
                .description("Noise cancelling")
                .price(new BigDecimal("99.99"))
                .stockQty(50)
                .category("Electronics")
                .imageUrl("headset.png")
                .build();

        Product persisted = new Product(10L, 2L, "Wireless Headset", "Noise cancelling",
                new BigDecimal("99.99"), 50, "Electronics", "headset.png", Timestamp.from(Instant.now()));

        when(productDAO.create(any(Product.class))).thenReturn(persisted);

        ProductDTO result = productService.createProduct(input, 2L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Wireless Headset", result.getName());
        assertEquals(2L, result.getSellerId());
        verify(productDAO).create(any(Product.class));
    }

    @Test
    @DisplayName("createProduct: fails on negative price or stock")
    void testCreateProductInvalidInputs() {
        ProductDTO badPrice = ProductDTO.builder()
                .name("Bad Item")
                .price(new BigDecimal("-10.00"))
                .stockQty(10)
                .category("Electronics")
                .build();

        assertThrows(ValidationException.class, () -> productService.createProduct(badPrice, 2L));

        ProductDTO badStock = ProductDTO.builder()
                .name("Bad Stock")
                .price(new BigDecimal("10.00"))
                .stockQty(-5)
                .category("Electronics")
                .build();

        assertThrows(ValidationException.class, () -> productService.createProduct(badStock, 2L));
    }

    @Test
    @DisplayName("updateProduct: seller can only update own products")
    void testUpdateProductSellerOwnership() throws Exception {
        Product existing = new Product(10L, 2L, "Original Name", "Desc",
                new BigDecimal("50.00"), 10, "Electronics", null, null);

        when(productDAO.findById(10L)).thenReturn(Optional.of(existing));

        ProductDTO updateDTO = ProductDTO.builder()
                .id(10L)
                .name("Hacked Name")
                .price(new BigDecimal("1.00"))
                .stockQty(5)
                .category("Electronics")
                .build();

        // Seller 99 attempts to modify Seller 2's product -> Forbidden
        assertThrows(ForbiddenException.class, () -> productService.updateProduct(updateDTO, 99L));
        verify(productDAO, never()).update(any());

        // Seller 2 updates successfully
        when(productDAO.update(any(Product.class))).thenReturn(true);
        ProductDTO successResult = productService.updateProduct(updateDTO, 2L);
        assertEquals("Hacked Name", successResult.getName());
        verify(productDAO).update(existing);
    }

    @Test
    @DisplayName("deleteProduct: seller deletes own product, admin deletes any, unauthorized rejected")
    void testDeleteProductAuthorization() throws Exception {
        Product p = new Product(10L, 2L, "Mouse", "Ergo", new BigDecimal("25.00"), 10, "Electronics", null, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(productDAO.delete(10L)).thenReturn(true);

        // 1. Unauthorized user (user 5, not admin)
        assertThrows(ForbiddenException.class, () -> productService.deleteProduct(10L, 5L, false));

        // 2. Owner seller (user 2, not admin)
        assertDoesNotThrow(() -> productService.deleteProduct(10L, 2L, false));

        // 3. Admin (user 999, admin = true)
        assertDoesNotThrow(() -> productService.deleteProduct(10L, 999L, true));
    }

    @Test
    @DisplayName("searchProductsWithFilters: returns accurate PagedResponse")
    void testSearchProductsWithFilters() throws Exception {
        Product p1 = new Product(1L, 2L, "Gaming Laptop", "Fast", new BigDecimal("1200.00"), 5, "Electronics", null, null);
        ProductFilter filter = ProductFilter.builder()
                .category("Electronics")
                .minPrice(new BigDecimal("1000.00"))
                .page(1)
                .size(10)
                .build();

        when(productDAO.findWithFilters(filter)).thenReturn(List.of(p1));
        when(productDAO.countFiltered(filter)).thenReturn(25);

        PagedResponse<ProductDTO> response = productService.searchProductsWithFilters(filter);
        assertEquals(1, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(25L, response.getTotalResults());
        assertEquals(3, response.getTotalPages()); // ceil(25 / 10) = 3
        assertEquals(1, response.getData().size());
        assertEquals("Gaming Laptop", response.getData().get(0).getName());
    }

    @Test
    @DisplayName("getProductById: throws ResourceNotFoundException when product absent")
    void testGetProductByIdNotFound() throws Exception {
        when(productDAO.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(404L));
    }
}
