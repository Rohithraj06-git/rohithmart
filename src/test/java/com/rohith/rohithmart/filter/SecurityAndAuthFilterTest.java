package com.rohith.rohithmart.filter;

import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.CsrfUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Security, CSRF, and AuthFilter Unit Tests")
class SecurityAndAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpSession session;

    @Test
    @DisplayName("SecurityHeadersFilter: attaches security response headers")
    void testSecurityHeaders() throws Exception {
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
        verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        verify(response).setHeader(eq("Content-Security-Policy"), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("CsrfFilter: safe GET method passes and binds csrf attribute if session present")
    void testCsrfSafeMethod() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(CsrfUtil.CSRF_SESSION_ATTR)).thenReturn("secret-csrf-token");

        filter.doFilter(request, response, filterChain);

        verify(request).setAttribute(eq(CsrfUtil.CSRF_SESSION_ATTR), eq("secret-csrf-token"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("CsrfFilter: POST with valid token header passes")
    void testCsrfPostValidToken() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/cart/add");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(CsrfUtil.CSRF_SESSION_ATTR)).thenReturn("valid-token");
        when(request.getHeader(CsrfUtil.CSRF_HEADER_NAME)).thenReturn("valid-token");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("CsrfFilter: POST with missing token in active session returns 403")
    void testCsrfPostMissingToken() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/cart/add");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(CsrfUtil.CSRF_SESSION_ATTR)).thenReturn("valid-token");
        when(request.getHeader(CsrfUtil.CSRF_HEADER_NAME)).thenReturn(null);
        when(request.getParameter(CsrfUtil.CSRF_PARAM_NAME)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter: Unauthenticated web request redirects to login")
    void testAuthFilterUnauthenticatedWeb() throws Exception {
        AuthFilter filter = new AuthFilter();
        when(request.getRequestURI()).thenReturn("/buyer/orders");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect("/auth/login");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter: Unauthenticated API request returns 401 JSON")
    void testAuthFilterUnauthenticatedApi() throws Exception {
        AuthFilter filter = new AuthFilter();
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter: Buyer role blocked with 403 when accessing /seller/*")
    void testAuthFilterBuyerAccessSellerBlocked() throws Exception {
        AuthFilter filter = new AuthFilter();
        when(request.getRequestURI()).thenReturn("/seller/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);

        User buyer = new User();
        buyer.setId(1L);
        buyer.setRole(Role.BUYER);
        when(session.getAttribute("user")).thenReturn(buyer);

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this area");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter: Buyer role blocked with 403 when accessing /admin/*")
    void testAuthFilterBuyerAccessAdminBlocked() throws Exception {
        AuthFilter filter = new AuthFilter();
        when(request.getRequestURI()).thenReturn("/admin/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);

        User buyer = new User();
        buyer.setId(1L);
        buyer.setRole(Role.BUYER);
        when(session.getAttribute("user")).thenReturn(buyer);

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this area");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter: Seller role allowed when accessing /seller/*")
    void testAuthFilterSellerAllowed() throws Exception {
        AuthFilter filter = new AuthFilter();
        when(request.getRequestURI()).thenReturn("/seller/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);

        User seller = new User();
        seller.setId(2L);
        seller.setRole(Role.SELLER);
        when(session.getAttribute("user")).thenReturn(seller);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter: Admin role allowed when accessing /admin/*")
    void testAuthFilterAdminAllowed() throws Exception {
        AuthFilter filter = new AuthFilter();
        when(request.getRequestURI()).thenReturn("/admin/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(session);

        User admin = new User();
        admin.setId(99L);
        admin.setRole(Role.ADMIN);
        when(session.getAttribute("user")).thenReturn(admin);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
