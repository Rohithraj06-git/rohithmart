package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.ReviewDTO;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.ReviewService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewApiServlet Tests")
class ReviewApiServletTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ReviewApiServlet servlet;
    private StringWriter responseWriter;
    private User testBuyer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ReviewApiServlet(reviewService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setRole(Role.BUYER);
    }

    @Test
    @DisplayName("GET /api/reviews?productId=10 returns 200 with reviews and avg rating")
    void testGetReviewsSuccess() throws Exception {
        when(request.getParameter("productId")).thenReturn("10");

        ReviewDTO review = new ReviewDTO(1L, 10L, 1L, "Buyer One", 5, "Excellent quality!", null);
        when(reviewService.getReviewsByProduct(10L)).thenReturn(List.of(review));
        when(reviewService.getAverageRating(10L)).thenReturn(5.0);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
        JsonObject data = json.getAsJsonObject("data");
        assertEquals(5.0, data.get("averageRating").getAsDouble());
    }

    @Test
    @DisplayName("POST /api/reviews submits review and returns 201")
    void testAddReviewSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(testBuyer);
        when(request.getContentType()).thenReturn("application/json");

        String body = "{\"productId\":10,\"rating\":5,\"comment\":\"Love it!\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        ReviewDTO created = new ReviewDTO(2L, 10L, 1L, "Buyer One", 5, "Love it!", null);
        when(reviewService.addReview(10L, 1L, 5, "Love it!")).thenReturn(created);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
    }
}
