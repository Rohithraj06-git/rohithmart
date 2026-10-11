package com.rohith.rohithmart.filter;

import com.rohith.rohithmart.dto.ApiResponse;
import com.rohith.rohithmart.util.CsrfUtil;
import com.rohith.rohithmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Set;

/**
 * Filter that enforces Cross-Site Request Forgery (CSRF) protection on state-modifying requests
 * (POST, PUT, DELETE, PATCH).
 *
 * For safe requests (GET, HEAD, OPTIONS, TRACE), attaches the current session's CSRF token
 * to request attributes so JSPs can render hidden input fields.
 */
public class CsrfFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String method = req.getMethod().toUpperCase();
        HttpSession session = req.getSession(false);

        // For safe methods, attach CSRF token to request attribute
        if (SAFE_METHODS.contains(method)) {
            if (session == null) {
                session = req.getSession(true);
            }
            if (session != null) {
                req.setAttribute(CsrfUtil.CSRF_SESSION_ATTR, CsrfUtil.getOrCreateToken(session));
            }
            chain.doFilter(request, response);
            return;
        }

        // For unsafe methods (POST, PUT, DELETE):
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // Skip CSRF validation for initial public entrypoints and H2 console
        if (path.startsWith("/h2-console") ||
            (isPublicAuthEndpoint(path) && (session == null || session.getAttribute(CsrfUtil.CSRF_SESSION_ATTR) == null))) {
            chain.doFilter(request, response);
            return;
        }

        // If session exists and has a token, validate it
        if (session != null && session.getAttribute(CsrfUtil.CSRF_SESSION_ATTR) != null) {
            if (!CsrfUtil.isValidToken(req)) {
                logger.warn("Security audit: CSRF token validation failed for [{}] {} from IP [{}]",
                        method, path, req.getRemoteAddr());
                handleCsrfFailure(req, res);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicAuthEndpoint(String path) {
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/register") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register");
    }

    private void handleCsrfFailure(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getRequestURI().contains("/api/")) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            ApiResponse<Void> response = ApiResponse.error("INVALID_CSRF", "Invalid or missing CSRF token");
            res.getWriter().write(JsonUtil.toJson(response));
        } else {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
        }
    }

    @Override
    public void destroy() {
    }
}
