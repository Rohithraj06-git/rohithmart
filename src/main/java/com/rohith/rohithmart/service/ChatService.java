package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dto.ChatResponseDTO;
import com.rohith.rohithmart.exception.AppException;

import javax.servlet.http.HttpSession;

/**
 * Service interface for AI assistant queries, rate-limiting, and question caching.
 */
public interface ChatService {

    /**
     * Processes an AI chat message with rate-limiting and response caching.
     *
     * @param userMessage user's input message
     * @param session user HTTP session for per-session guardrails
     * @return ChatResponseDTO with reply
     * @throws AppException if rate limit exceeded or message invalid
     */
    ChatResponseDTO ask(String userMessage, HttpSession session) throws AppException;
}
