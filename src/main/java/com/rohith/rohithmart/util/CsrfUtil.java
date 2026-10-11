package com.rohith.rohithmart.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for generating, attaching, and verifying cryptographically secure CSRF tokens.
 */
public final class CsrfUtil {

    public static final String CSRF_SESSION_ATTR = "_csrf_token";
    public static final String CSRF_PARAM_NAME = "_csrf";
    public static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES_LENGTH = 32;

    private CsrfUtil() {
    }

    /**
     * Generates a new secure random CSRF token, stores it in the session, and returns it.
     *
     * @param session current HTTP session
     * @return generated CSRF token string
     */
    public static String generateToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        byte[] buffer = new byte[TOKEN_BYTES_LENGTH];
        SECURE_RANDOM.nextBytes(buffer);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
        session.setAttribute(CSRF_SESSION_ATTR, token);
        return token;
    }

    /**
     * Retrieves the current CSRF token from the session, or generates one if absent.
     *
     * @param session current HTTP session
     * @return CSRF token string
     */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        String token = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (token == null || token.trim().isEmpty()) {
            token = generateToken(session);
        }
        return token;
    }

    /**
     * Validates incoming request token against the session-stored token using constant-time comparison.
     * Checks header 'X-CSRF-TOKEN' first, then parameter '_csrf' or 'csrf_token'.
     *
     * @param request HTTP servlet request
     * @return true if token is valid, false otherwise
     */
    public static boolean isValidToken(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }

        String requestToken = request.getHeader(CSRF_HEADER_NAME);
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = request.getParameter(CSRF_PARAM_NAME);
        }
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = request.getParameter("csrf_token");
        }

        if (requestToken == null) {
            return false;
        }

        return MessageDigest.isEqual(
                sessionToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                requestToken.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
