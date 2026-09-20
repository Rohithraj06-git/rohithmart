package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.ApiResponse;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base abstract servlet providing standardized rendering, JSON API serialization,
 * and error handling utilities for thin controllers.
 */
public abstract class BaseServlet extends HttpServlet {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Forwards request to an internal JSP view under /WEB-INF/jsp/.
     */
    protected void forward(HttpServletRequest req, HttpServletResponse res, String jspPath)
            throws ServletException, IOException {
        String path = "/WEB-INF/jsp/" + (jspPath.startsWith("/") ? jspPath.substring(1) : jspPath);
        req.getRequestDispatcher(path).forward(req, res);
    }

    /**
     * Writes standard successful JSON response adhering to Capstone Section 13.
     */
    protected <T> void sendJsonResponse(HttpServletResponse res, int statusCode, T data) throws IOException {
        res.setStatus(statusCode);
        res.setContentType("application/json;charset=UTF-8");
        ApiResponse<T> envelope = ApiResponse.success(data);
        res.getWriter().write(JsonUtil.toJson(envelope));
    }

    /**
     * Writes standard error JSON response adhering to Capstone Section 13.
     */
    protected void sendJsonError(HttpServletResponse res, int statusCode, String code, String message) throws IOException {
        res.setStatus(statusCode);
        res.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> envelope = ApiResponse.error(code, message);
        res.getWriter().write(JsonUtil.toJson(envelope));
    }

    /**
     * Handles exceptions uniformly across servlets.
     */
    protected void handleException(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException, ServletException {
        logger.error("Error processing request: {}", e.getMessage(), e);
        if (req.getRequestURI().contains("/api/")) {
            if (e instanceof AppException) {
                AppException ae = (AppException) e;
                sendJsonError(res, ae.getStatusCode(), "APPLICATION_ERROR", ae.getMessage());
            } else {
                sendJsonError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR", "An unexpected error occurred");
            }
        } else {
            req.setAttribute("errorMessage", e.getMessage());
            if (e instanceof AppException) {
                res.setStatus(((AppException) e).getStatusCode());
            } else {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
