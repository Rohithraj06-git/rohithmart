package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dao.ReviewDAO;
import com.rohith.rohithmart.dao.UserDAO;
import com.rohith.rohithmart.dto.ReviewDTO;
import com.rohith.rohithmart.exception.ConflictException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Product;
import com.rohith.rohithmart.model.Review;
import com.rohith.rohithmart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests (Mockito)")
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private ProductDAO productDAO;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewDAO, userDAO, productDAO);
    }

    @Test
    @DisplayName("addReview: success when product exists, rating valid, and no duplicate review")
    void testAddReviewSuccess() throws Exception {
        Product p = new Product(10L, 2L, "Earbuds", "Wireless", new BigDecimal("49.99"), 10, "Electronics", null, null);
        User u = new User(1L, "Alice Buyer", "alice@example.com", "hash", null, null);
        Review created = new Review(100L, 10L, 1L, 5, "Amazing sound quality!", Timestamp.from(Instant.now()));

        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(reviewDAO.hasUserReviewed(1L, 10L)).thenReturn(false);
        when(reviewDAO.create(any(Review.class))).thenReturn(created);
        when(userDAO.findById(1L)).thenReturn(Optional.of(u));

        ReviewDTO dto = reviewService.addReview(10L, 1L, 5, "Amazing sound quality!");
        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals(5, dto.getRating());
        assertEquals("Amazing sound quality!", dto.getComment());
        assertEquals("Alice Buyer", dto.getUserName());
        verify(reviewDAO).create(any(Review.class));
    }

    @Test
    @DisplayName("addReview: fails when product does not exist")
    void testAddReviewProductNotFound() throws Exception {
        when(productDAO.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reviewService.addReview(999L, 1L, 4, "Good"));
        verify(reviewDAO, never()).create(any());
    }

    @Test
    @DisplayName("addReview: fails when user already reviewed the product")
    void testAddReviewDuplicate() throws Exception {
        Product p = new Product(10L, 2L, "Earbuds", "Wireless", new BigDecimal("49.99"), 10, "Electronics", null, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(reviewDAO.hasUserReviewed(1L, 10L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> reviewService.addReview(10L, 1L, 4, "Trying again"));
        verify(reviewDAO, never()).create(any());
    }

    @Test
    @DisplayName("addReview: fails on invalid rating (< 1 or > 5)")
    void testAddReviewInvalidRating() {
        assertThrows(ValidationException.class, () -> reviewService.addReview(10L, 1L, 0, "Too low"));
        assertThrows(ValidationException.class, () -> reviewService.addReview(10L, 1L, 6, "Too high"));
        assertThrows(ValidationException.class, () -> reviewService.addReview(10L, 1L, -1, "Negative"));
    }

    @Test
    @DisplayName("getAverageRating: returns calculated average from DAO")
    void testGetAverageRating() throws Exception {
        when(reviewDAO.calculateAverageRating(10L)).thenReturn(4.7);
        double avg = reviewService.getAverageRating(10L);
        assertEquals(4.7, avg, 0.01);
    }

    @Test
    @DisplayName("hasUserReviewed: delegates check to DAO")
    void testHasUserReviewed() throws Exception {
        when(reviewDAO.hasUserReviewed(1L, 10L)).thenReturn(true);
        assertTrue(reviewService.hasUserReviewed(1L, 10L));
    }
}
