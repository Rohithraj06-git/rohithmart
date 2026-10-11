package com.rohith.rohithmart.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that attaches security headers to all HTTP responses to prevent common web vulnerabilities:
 * - Anti-Clickjacking: X-Frame-Options: DENY
 * - Anti-MIME Sniffing: X-Content-Type-Options: nosniff
 * - XSS Filter: X-XSS-Protection: 1; mode=block
 * - Referrer Policy: Referrer-Policy: strict-origin-when-cross-origin
 * - Content Security Policy (CSP)
 */
public class SecurityHeadersFilter implements Filter {

    private static final String CSP_POLICY =
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "font-src 'self' https://cdnjs.cloudflare.com; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self';";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse res = (HttpServletResponse) response;
            boolean isH2Console = false;
            if (request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest) request;
                String uri = req.getRequestURI();
                if (uri != null && uri.contains("/h2-console")) {
                    isH2Console = true;
                }
            }

            if (isH2Console) {
                res.setHeader("X-Frame-Options", "SAMEORIGIN");
            } else {
                res.setHeader("X-Content-Type-Options", "nosniff");
                res.setHeader("X-Frame-Options", "DENY");
                res.setHeader("X-XSS-Protection", "1; mode=block");
                res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                res.setHeader("Content-Security-Policy", CSP_POLICY);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
