package com.rohith.rohithmart.ai;

import com.rohith.rohithmart.util.ConfigUtil;

/**
 * Factory providing the configured ChatProvider instance.
 */
public class ChatProviderFactory {

    private static final ChatProvider MOCK_PROVIDER = new MockChatProvider();
    private static final ChatProvider GEMINI_PROVIDER = new GeminiChatProvider();

    private ChatProviderFactory() {
    }

    public static ChatProvider getProvider() {
        String providerType = ConfigUtil.get("ai.chatbot.provider", "mock");
        if ("gemini".equalsIgnoreCase(providerType)) {
            return GEMINI_PROVIDER;
        }
        return MOCK_PROVIDER;
    }
}
