package com.rohith.rohithmart.ai;

/**
 * Interface for AI chat providers as specified in Capstone Section 17.
 */
public interface ChatProvider {

    /**
     * Generates an assistant response for the user inquiry.
     *
     * @param userMessage user's input message
     * @param context marketplace or product context
     * @return reply string
     */
    String getReply(String userMessage, String context);
}
