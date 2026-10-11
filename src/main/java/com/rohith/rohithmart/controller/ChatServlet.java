package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.ChatRequestDTO;
import com.rohith.rohithmart.dto.ChatResponseDTO;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.service.ChatService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AI assistant endpoint maintaining server-side provider abstraction:
 * POST /api/chat { "message": "..." } -> Standard ApiResponse with ChatResponseDTO payload.
 */
@WebServlet(name = "ChatServlet", urlPatterns = {"/api/chat"})
public class ChatServlet extends BaseServlet {

    private final ChatService chatService;

    public ChatServlet() {
        this(ServiceFactory.getChatService());
    }

    public ChatServlet(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            ChatRequestDTO requestDTO = parseJson(req, ChatRequestDTO.class);
            if (requestDTO == null || !ValidationUtil.isNotEmpty(requestDTO.getMessage())) {
                throw new ValidationException("Message is required", "message", "Message cannot be empty");
            }

            HttpSession session = req.getSession(true);
            ChatResponseDTO responseDTO = chatService.ask(requestDTO.getMessage(), session);

            sendJsonResponse(res, HttpServletResponse.SC_OK, responseDTO);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in ChatServlet", ex);
            }
        }
    }
}
