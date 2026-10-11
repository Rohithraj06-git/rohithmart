package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.ReviewDTO;
import com.rohith.rohithmart.dto.ReviewRequest;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.ReviewService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for product reviews and ratings.
 *
 * Endpoints:
 * - GET  /api/reviews?productId={id}     (retrieve reviews and average rating for a product)
 * - POST /api/reviews                    (submit review and star rating)
 */
@WebServlet(name = "ReviewApiServlet", urlPatterns = {"/api/reviews", "/api/reviews/*"})
public class ReviewApiServlet extends BaseServlet {

    private final ReviewService reviewService;

    public ReviewApiServlet() {
        this(ServiceFactory.getReviewService());
    }

    public ReviewApiServlet(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String productIdStr = req.getParameter("productId");
            if (productIdStr == null || productIdStr.trim().isEmpty()) {
                throw new ValidationException("Parameter 'productId' is required", "productId", "productId cannot be blank");
            }
            Long productId = ValidationUtil.validateId(productIdStr, "productId");

            List<ReviewDTO> reviews = reviewService.getReviewsByProduct(productId);
            double averageRating = reviewService.getAverageRating(productId);

            Map<String, Object> reviewData = new LinkedHashMap<>();
            reviewData.put("productId", productId);
            reviewData.put("averageRating", averageRating);
            reviewData.put("totalReviews", reviews.size());
            reviewData.put("reviews", reviews);

            sendJsonResponse(res, HttpServletResponse.SC_OK, reviewData);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in ReviewApiServlet doGet", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);

            Long productId = null;
            Integer rating = null;
            String comment = null;

            if (req.getContentType() != null && req.getContentType().contains("application/json")) {
                ReviewRequest requestDTO = parseJson(req, ReviewRequest.class);
                if (requestDTO != null) {
                    productId = requestDTO.getProductId();
                    rating = requestDTO.getRating();
                    comment = requestDTO.getComment();
                }
            } else {
                String pId = req.getParameter("productId");
                String rVal = req.getParameter("rating");
                comment = req.getParameter("comment");
                if (ValidationUtil.isNotEmpty(pId)) productId = Long.parseLong(pId.trim());
                if (ValidationUtil.isNotEmpty(rVal)) rating = Integer.parseInt(rVal.trim());
            }

            if (productId == null) {
                throw new ValidationException("Product ID is required", "productId", "productId cannot be null");
            }
            if (rating == null) {
                throw new ValidationException("Rating is required", "rating", "rating cannot be null");
            }

            ValidationUtil.validateRating(rating);
            comment = ValidationUtil.validateComment(comment);

            ReviewDTO createdReview = reviewService.addReview(productId, user.getId(), rating, comment);
            sendJsonResponse(res, HttpServletResponse.SC_CREATED, "Review submitted successfully", createdReview);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in ReviewApiServlet doPost", ex);
            }
        }
    }
}
