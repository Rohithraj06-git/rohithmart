package com.rohith.rohithmart.dto;

/**
 * Response payload for AI Chatbot endpoint.
 */
public class ChatResponseDTO {
    private String reply;

    public ChatResponseDTO() {
    }

    public ChatResponseDTO(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
