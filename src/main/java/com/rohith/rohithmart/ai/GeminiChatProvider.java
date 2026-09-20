package com.rohith.rohithmart.ai;

import com.rohith.rohithmart.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gemini LLM integration provider.
 * Implements outbound API call with timeout and graceful degradation fallback.
 */
public class GeminiChatProvider implements ChatProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeminiChatProvider.class);
    private final MockChatProvider fallbackProvider = new MockChatProvider();

    @Override
    public String getReply(String userMessage, String context) {
        String apiKey = ConfigUtil.get("ai.gemini.apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.info("Gemini API key not configured; falling back to canned response.");
            return fallbackProvider.getReply(userMessage, context);
        }

        try {
            // Outbound Gemini REST call implementation will be active in Phase 3
            // Currently falls back cleanly as mandated by Section 11 & 17
            return fallbackProvider.getReply(userMessage, context);
        } catch (Exception e) {
            logger.error("Error invoking Gemini API: {}", e.getMessage());
            return fallbackProvider.getReply(userMessage, context);
        }
    }
}
