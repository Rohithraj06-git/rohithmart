package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.exception.AppException;

import java.util.List;

/**
 * Service interface for product management, search, and catalog operations.
 */
public interface ProductService {

    /**
     * Retrieves product details by ID.
     *
     * @param id product ID
     * @return ProductDTO
     * @throws AppException if not found
     */
    ProductDTO getProductById(Long id) throws AppException;

    /**
     * Searches products with category, keyword, and sorting filters.
     *
     * @param category category filter
     * @param keyword search keyword
     * @param sortBy sort order
     * @param page 1-indexed page number
     * @param pageSize page size
     * @return list of ProductDTOs
     * @throws AppException on processing failure
     */
    List<ProductDTO> searchProducts(String category, String keyword, String sortBy, int page, int pageSize) throws AppException;

    /**
     * Executes advanced search with multi-criteria filtering and returns pagination metadata.
     *
     * @param filter product filter criteria
     * @return PagedResponse containing products, totalPages, totalResults, and page metadata
     * @throws AppException on processing failure
     */
    com.rohith.rohithmart.dto.PagedResponse<ProductDTO> searchProductsWithFilters(com.rohith.rohithmart.dto.ProductFilter filter) throws AppException;

    /**
     * Retrieves products belonging to a specific category.
     *
     * @param category category name
     * @return list of ProductDTOs
     * @throws AppException on processing failure
     */
    List<ProductDTO> getProductsByCategory(String category) throws AppException;

    /**
     * Retrieves all products created by a specific seller.
     *
     * @param sellerId seller's user ID
     * @return list of ProductDTOs
     * @throws AppException on processing failure
     */
    List<ProductDTO> getProductsBySeller(Long sellerId) throws AppException;

    /**
     * Creates a new product listing.
     *
     * @param dto product data
     * @param sellerId seller ID
     * @return created ProductDTO
     * @throws AppException if validation fails
     */
    ProductDTO createProduct(ProductDTO dto, Long sellerId) throws AppException;

    /**
     * Updates an existing product listing.
     *
     * @param dto updated product data
     * @param sellerId seller ID
     * @return updated ProductDTO
     * @throws AppException if validation fails or unauthorized
     */
    ProductDTO updateProduct(ProductDTO dto, Long sellerId) throws AppException;

    /**
     * Updates an existing product listing with administrator override capability.
     *
     * @param dto updated product data
     * @param userId user ID (seller or admin)
     * @param isAdmin whether user has admin privileges
     * @return updated ProductDTO
     * @throws AppException if validation fails or unauthorized
     */
    default ProductDTO updateProduct(ProductDTO dto, Long userId, boolean isAdmin) throws AppException {
        return updateProduct(dto, userId);
    }

    /**
     * Deletes a product listing.
     *
     * @param productId product ID
     * @param userId user requesting deletion
     * @param isAdmin whether user has admin privileges
     * @throws AppException if unauthorized or not found
     */
    void deleteProduct(Long productId, Long userId, boolean isAdmin) throws AppException;

    /**
     * Returns total count of products in marketplace.
     *
     * @return product count
     * @throws AppException on failure
     */
    int getTotalProductCount() throws AppException;
}
