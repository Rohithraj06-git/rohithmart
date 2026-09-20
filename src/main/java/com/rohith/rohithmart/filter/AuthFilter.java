package com.rohith.rohithmart.filter;

import com.rohith.rohithmart.dto.ApiResponse;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.JsonUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Enforces session-based authentication and role-based access control (RBAC).
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        boolean isApi = path.startsWith("/api/");

        // Check if user is logged in
        if (user == null) {
            if (isApi) {
                sendJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Authentication required");
            } else {
                res.sendRedirect(contextPath + "/auth/login");
            }
            return;
        }

        // Role-based authorization
        if (path.startsWith("/seller") && user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
            handleForbidden(res, isApi, contextPath);
            return;
        }

        if (path.startsWith("/admin") && user.getRole() != Role.ADMIN) {
            handleForbidden(res, isApi, contextPath);
            return;
        }

        chain.doFilter(request, response);
    }

    private void handleForbidden(HttpServletResponse res, boolean isApi, String contextPath) throws IOException {
        if (isApi) {
            sendJsonError(res, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Insufficient role permissions");
        } else {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this area");
        }
    }

    private void sendJsonError(HttpServletResponse res, int status, String code, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> errorResp = ApiResponse.error(code, message);
        res.getWriter().write(JsonUtil.toJson(errorResp));
    }

    @Override
    public void destroy() {
    }
}
