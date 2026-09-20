package com.rohith.rohithmart.service;

import com.rohith.rohithmart.ai.ChatProvider;
import com.rohith.rohithmart.ai.ChatProviderFactory;
import com.rohith.rohithmart.dto.ChatResponseDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of ChatService enforcing Section 17 guardrails:
 * - 10 messages/minute per session rate limit
 * - Input length cap (300 chars)
 * - Per-session question response caching
 * - Graceful degradation fallback
 */
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    private static final int MAX_INPUT_LENGTH = 300;
    private static final int RATE_LIMIT_MAX_REQUESTS = 10;
    private static final long RATE_LIMIT_WINDOW_MS = 60000; // 1 minute

    private static final String SESSION_CHAT_TIMESTAMPS = "CHAT_TIMESTAMPS";
    private static final String SESSION_CHAT_CACHE = "CHAT_CACHE";

    @Override
    public ChatResponseDTO ask(String userMessage, HttpSession session) throws AppException {
        // 1. Validate Input
        if (!ValidationUtil.isNotEmpty(userMessage)) {
            throw new ValidationException("Message cannot be empty");
        }
        String cleanMsg = userMessage.trim();
        if (cleanMsg.length() > MAX_INPUT_LENGTH) {
            throw new ValidationException("Message exceeds maximum allowed length of " + MAX_INPUT_LENGTH + " characters");
        }

        // 2. Guardrail: Enforce per-session rate limit
        if (session != null) {
            checkRateLimit(session);
        }

        // 3. Guardrail: Check per-session cache for repeated identical queries
        String cacheKey = cleanMsg.toLowerCase(Locale.ROOT);
        if (session != null) {
            Map<String, String> cache = getSessionCache(session);
            if (cache.containsKey(cacheKey)) {
                logger.debug("Serving AI response from session cache for query: {}", cleanMsg);
                return new ChatResponseDTO(cache.get(cacheKey));
            }
        }

        // 4. Invoke Provider
        String reply;
        try {
            ChatProvider provider = ChatProviderFactory.getProvider();
            reply = provider.getReply(cleanMsg, "RohithMart marketplace context");
        } catch (Exception e) {
            logger.error("Chat provider failed; falling back to degraded response", e);
            reply = "I apologize, but I am temporarily experiencing connection difficulties. Please browse our categories or contact support.";
        }

        // 5. Cache reply in session
        if (session != null) {
            getSessionCache(session).put(cacheKey, reply);
        }

        return new ChatResponseDTO(reply);
    }

    @SuppressWarnings("unchecked")
    private synchronized void checkRateLimit(HttpSession session) throws AppException {
        Deque<Long> timestamps = (Deque<Long>) session.getAttribute(SESSION_CHAT_TIMESTAMPS);
        if (timestamps == null) {
            timestamps = new ArrayDeque<>();
            session.setAttribute(SESSION_CHAT_TIMESTAMPS, timestamps);
        }

        long now = System.currentTimeMillis();
        // Evict expired timestamps
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst() > RATE_LIMIT_WINDOW_MS)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= RATE_LIMIT_MAX_REQUESTS) {
            logger.warn("Per-session rate limit exceeded for session ID: {}", session.getId());
            throw new AppException("Rate limit exceeded. Please wait a minute before sending another question.", 429);
        }

        timestamps.addLast(now);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getSessionCache(HttpSession session) {
        Map<String, String> cache = (Map<String, String>) session.getAttribute(SESSION_CHAT_CACHE);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            session.setAttribute(SESSION_CHAT_CACHE, cache);
        }
        return cache;
    }
}
