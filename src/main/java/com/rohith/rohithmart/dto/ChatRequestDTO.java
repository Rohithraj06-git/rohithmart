package com.rohith.rohithmart.dto;

/**
 * Request payload for AI Chatbot endpoint.
 */
public class ChatRequestDTO {
    private String message;

    public ChatRequestDTO() {
    }

    public ChatRequestDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
