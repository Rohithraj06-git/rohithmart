package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.ChatRequestDTO;
import com.rohith.rohithmart.dto.ChatResponseDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.service.ChatService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AI assistant endpoint as specified in Capstone Section 11 and 17:
 * POST /api/chat { "message": "..." } -> { "reply": "..." }
 */
@WebServlet(name = "ChatServlet", urlPatterns = {"/api/chat"})
public class ChatServlet extends BaseServlet {

    private final ChatService chatService = ServiceFactory.getChatService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");

        try {
            ChatRequestDTO requestDTO = JsonUtil.fromJson(req.getReader(), ChatRequestDTO.class);
            if (requestDTO == null || requestDTO.getMessage() == null) {
                sendJsonError(res, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Message is required");
                return;
            }

            HttpSession session = req.getSession(true);
            ChatResponseDTO responseDTO = chatService.ask(requestDTO.getMessage(), session);

            res.getWriter().write(JsonUtil.toJson(responseDTO));
        } catch (AppException e) {
            sendJsonError(res, e.getStatusCode(), "CHAT_ERROR", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing AI chat request", e);
            sendJsonError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR", "Chat service unavailable");
        }
    }
}
