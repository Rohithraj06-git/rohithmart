package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.ChatResponseDTO;
import com.rohith.rohithmart.service.ChatService;
import com.rohith.rohithmart.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatServlet Tests")
class ChatServletTest {

    @Mock
    private ChatService chatService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ChatServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ChatServlet(chatService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("POST /api/chat returns 200 with AI assistant response")
    void testChatSuccess() throws Exception {
        when(request.getSession(true)).thenReturn(session);
        String requestJson = "{\"message\":\"Can you recommend a laptop?\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestJson)));

        ChatResponseDTO reply = new ChatResponseDTO("We have excellent laptops in our Electronics category!");
        when(chatService.ask(eq("Can you recommend a laptop?"), any(HttpSession.class))).thenReturn(reply);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(json.get("success").getAsBoolean());
        JsonObject data = json.getAsJsonObject("data");
        assertEquals("We have excellent laptops in our Electronics category!", data.get("reply").getAsString());
    }

    @Test
    @DisplayName("POST /api/chat with blank message returns 400 VALIDATION_ERROR")
    void testChatBlankMessage() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/chat");
        String requestJson = "{\"message\":\"   \"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestJson)));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject json = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertFalse(json.get("success").getAsBoolean());
        assertEquals("VALIDATION_ERROR", json.get("errorCode").getAsString());
    }
}
