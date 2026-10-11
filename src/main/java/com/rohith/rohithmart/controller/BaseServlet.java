package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.ApiResponse;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.AuthenticationException;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Base abstract servlet providing standardized rendering, JSON API serialization,
 * session authentication extraction, and centralized error handling for thin controllers.
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
     * Writes standard successful JSON response with default message.
     */
    protected <T> void sendJsonResponse(HttpServletResponse res, int statusCode, T data) throws IOException {
        sendJsonResponse(res, statusCode, "Operation successful", data);
    }

    /**
     * Writes standard successful JSON response adhering to Capstone requirements:
     * { "success": true, "message": "...", "data": { ... } }
     */
    protected <T> void sendJsonResponse(HttpServletResponse res, int statusCode, String message, T data) throws IOException {
        res.setStatus(statusCode);
        res.setContentType("application/json;charset=UTF-8");
        ApiResponse<T> envelope = ApiResponse.success(message, data);
        res.getWriter().write(JsonUtil.toJson(envelope));
    }

    /**
     * Writes standard error JSON response adhering to Capstone requirements:
     * { "success": false, "message": "...", "errorCode": "..." }
     */
    protected void sendJsonError(HttpServletResponse res, int statusCode, String errorCode, String message) throws IOException {
        res.setStatus(statusCode);
        res.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> envelope = ApiResponse.error(errorCode, message);
        res.getWriter().write(JsonUtil.toJson(envelope));
    }

    /**
     * Parses request JSON body using Gson.
     */
    protected <T> T parseJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        return JsonUtil.fromJson(req.getReader(), clazz);
    }

    /**
     * Retrieves currently authenticated user from session if present.
     */
    protected User getAuthenticatedUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("user") : null;
    }

    /**
     * Requires an authenticated session, otherwise throwing AuthenticationException (HTTP 401).
     */
    protected User requireAuthenticatedUser(HttpServletRequest req) throws AppException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            throw new AuthenticationException("Authentication required to access this resource");
        }
        return user;
    }
    /**
     * Centralized exception-to-response handling across all servlets.
     */
    protected void handleException(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException, ServletException {
        String uri = req.getRequestURI();
        logger.error("Error processing request [{}]: {}", uri, e.getMessage(), e);
        if (uri != null && uri.contains("/api/")) {
            if (e instanceof AppException) {
                AppException ae = (AppException) e;
                sendJsonError(res, ae.getStatusCode(), ae.getErrorCode(), ae.getMessage());
            } else if (e instanceof IllegalArgumentException || e instanceof NumberFormatException) {
                sendJsonError(res, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
            } else {
                sendJsonError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR", "An unexpected error occurred");
            }
        } else {
            req.setAttribute("errorMessage", e.getMessage());
            if (e instanceof AppException) {
                res.setStatus(((AppException) e).getStatusCode());
            } else if (e instanceof IllegalArgumentException || e instanceof NumberFormatException) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
