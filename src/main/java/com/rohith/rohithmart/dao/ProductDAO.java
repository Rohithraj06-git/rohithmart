package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for products.
 */
public interface ProductDAO {

    /**
     * Finds a product by its unique primary key.
     *
     * @param id product ID
     * @return Optional containing product if found
     * @throws SQLException on database failure
     */
    Optional<Product> findById(Long id) throws SQLException;

    /**
     * Searches and filters products with pagination and sorting.
     *
     * @param category category filter (optional)
     * @param keyword search keyword (optional)
     * @param sortBy sort order (optional)
     * @param limit maximum results to return
     * @param offset starting record offset
     * @return list of matching products
     * @throws SQLException on database failure
     */
    List<Product> search(String category, String keyword, String sortBy, int limit, int offset) throws SQLException;

    /**
     * Retrieves all products listed by a given seller.
     *
     * @param sellerId seller's user ID
     * @return list of products
     * @throws SQLException on database failure
     */
    List<Product> findBySellerId(Long sellerId) throws SQLException;

    /**
     * Inserts a new product record.
     *
     * @param product product entity to insert
     * @return created product with generated ID
     * @throws SQLException on database failure
     */
    Product create(Product product) throws SQLException;

    /**
     * Updates an existing product.
     *
     * @param product product entity with updated fields
     * @return true if updated, false otherwise
     * @throws SQLException on database failure
     */
    boolean update(Product product) throws SQLException;

    /**
     * Deletes a product by ID.
     *
     * @param id product ID
     * @return true if deleted, false otherwise
     * @throws SQLException on database failure
     */
    boolean delete(Long id) throws SQLException;

    /**
     * Decrements product stock within an existing transaction connection.
     *
     * @param conn transaction database connection
     * @param productId product ID
     * @param quantity quantity to deduct
     * @return true if stock was successfully reduced
     * @throws SQLException on database failure
     */
    boolean deductStock(Connection conn, Long productId, int quantity) throws SQLException;

    /**
     * Counts total number of products.
     *
     * @return product count
     * @throws SQLException on database failure
     */
    int countAll() throws SQLException;
}
