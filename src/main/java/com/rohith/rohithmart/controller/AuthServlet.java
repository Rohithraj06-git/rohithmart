package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.LoginRequestDTO;
import com.rohith.rohithmart.dto.RegisterRequestDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.AuthService;
import com.rohith.rohithmart.service.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller managing user authentication, registration, login, logout,
 * session renewal, and timeout settings.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*"})
public class AuthServlet extends BaseServlet {

    private final AuthService authService = ServiceFactory.getAuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if ("/login".equals(pathInfo)) {
            forward(req, res, "auth/login.jsp");
        } else if ("/register".equals(pathInfo)) {
            forward(req, res, "auth/register.jsp");
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(req, res);
        } else {
            res.sendRedirect(req.getContextPath() + "/auth/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if ("/login".equals(pathInfo)) {
                handleLogin(req, res);
            } else if ("/register".equals(pathInfo)) {
                handleRegister(req, res);
            } else {
                res.sendRedirect(req.getContextPath() + "/auth/login");
            }
        } catch (Exception e) {
            handleException(req, res, e);
            if ("/login".equals(pathInfo)) {
                forward(req, res, "auth/login.jsp");
            } else {
                forward(req, res, "auth/register.jsp");
            }
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse res) throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(req.getParameter("email"));
        dto.setPassword(req.getParameter("password"));

        User user = authService.login(dto);

        // Capstone Rule 3: Session management via HttpSession. Regenerate session ID on login.
        req.changeSessionId();
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes explicit timeout
        session.setAttribute("user", user);

        if (user.getRole() == Role.ADMIN) {
            res.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else if (user.getRole() == Role.SELLER) {
            res.sendRedirect(req.getContextPath() + "/seller/dashboard");
        } else {
            res.sendRedirect(req.getContextPath() + "/home");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse res) throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setName(req.getParameter("name"));
        dto.setEmail(req.getParameter("email"));
        dto.setPassword(req.getParameter("password"));

        String roleParam = req.getParameter("role");
        dto.setRole("SELLER".equalsIgnoreCase(roleParam) ? Role.SELLER : Role.BUYER);

        UserResponseDTO created = authService.register(dto);
        logger.info("New registration completed for email: {}", created.getEmail());

        HttpSession session = req.getSession(true);
        session.setAttribute("flashSuccess", "Registration successful! Please sign in with your credentials.");
        res.sendRedirect(req.getContextPath() + "/auth/login");
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        res.sendRedirect(req.getContextPath() + "/auth/login");
    }
}
