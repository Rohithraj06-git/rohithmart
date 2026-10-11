package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.LoginRequestDTO;
import com.rohith.rohithmart.dto.RegisterRequestDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.AuthService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.CsrfUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * REST API Controller for authentication and current session state.
 *
 * Endpoints:
 * - POST /api/auth/login       (authenticate credentials and create secure session)
 * - POST /api/auth/register    (register new customer or seller account)
 * - POST /api/auth/logout      (invalidate session)
 * - GET  /api/auth/me          (retrieve current authenticated user profile)
 */
@WebServlet(name = "AuthApiServlet", urlPatterns = {"/api/auth/*"})
public class AuthApiServlet extends BaseServlet {

    private final AuthService authService;

    public AuthApiServlet() {
        this(ServiceFactory.getAuthService());
    }

    public AuthApiServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if ("/me".equals(pathInfo)) {
                User user = requireAuthenticatedUser(req);
                UserResponseDTO userDTO = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
                sendJsonResponse(res, HttpServletResponse.SC_OK, userDTO);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found");
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in AuthApiServlet doGet", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if ("/login".equals(pathInfo)) {
                handleLogin(req, res);
            } else if ("/register".equals(pathInfo)) {
                handleRegister(req, res);
            } else if ("/logout".equals(pathInfo)) {
                handleLogout(req, res);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found");
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in AuthApiServlet doPost", ex);
            }
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse res) throws Exception {
        LoginRequestDTO dto;
        if (req.getContentType() != null && req.getContentType().contains("application/json")) {
            dto = parseJson(req, LoginRequestDTO.class);
        } else {
            dto = new LoginRequestDTO();
            dto.setEmail(req.getParameter("email"));
            dto.setPassword(req.getParameter("password"));
        }

        if (dto == null) {
            throw new ValidationException("Login payload is required");
        }

        User user = authService.login(dto);

        // Prevent session fixation
        req.changeSessionId();
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
        session.setAttribute("user", user);

        // Bind fresh CSRF token
        CsrfUtil.generateToken(session);

        UserResponseDTO responseDTO = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
        sendJsonResponse(res, HttpServletResponse.SC_OK, "Login successful", responseDTO);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse res) throws Exception {
        RegisterRequestDTO dto;
        if (req.getContentType() != null && req.getContentType().contains("application/json")) {
            dto = parseJson(req, RegisterRequestDTO.class);
        } else {
            dto = new RegisterRequestDTO();
            dto.setName(req.getParameter("name"));
            dto.setEmail(req.getParameter("email"));
            dto.setPassword(req.getParameter("password"));
            String role = req.getParameter("role");
            if (role != null) {
                dto.setRole("SELLER".equalsIgnoreCase(role) ? com.rohith.rohithmart.model.Role.SELLER : com.rohith.rohithmart.model.Role.BUYER);
            }
        }

        if (dto == null) {
            throw new ValidationException("Registration payload is required");
        }

        UserResponseDTO responseDTO = authService.register(dto);
        sendJsonResponse(res, HttpServletResponse.SC_CREATED, "Registration successful", responseDTO);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        sendJsonResponse(res, HttpServletResponse.SC_OK, "Logout successful", null);
    }
}
