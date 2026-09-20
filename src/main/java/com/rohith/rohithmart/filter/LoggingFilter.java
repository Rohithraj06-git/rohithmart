package com.rohith.rohithmart.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter generating a unique request ID for each request, bound to SLF4J MDC.
 * Satisfies Capstone Section 18 structured observability requirement.
 */
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String MDC_REQUEST_ID_KEY = "requestId";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String requestId = req.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        res.setHeader(HEADER_REQUEST_ID, requestId);

        long start = System.currentTimeMillis();
        try {
            logger.debug("Request started: {} {}", req.getMethod(), req.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            logger.debug("Request completed: {} {} - Status: {} ({}ms)",
                    req.getMethod(), req.getRequestURI(), res.getStatus(), duration);
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    @Override
    public void destroy() {
    }
}
