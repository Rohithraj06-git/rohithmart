package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.LoginRequestDTO;
import com.rohith.rohithmart.dto.RegisterRequestDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AuthenticationException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.AuthService;
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
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApiServlet Tests")
class AuthApiServletTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private AuthApiServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AuthApiServlet(authService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("POST /api/auth/login sets session interval, changes session id, and returns 200")
    void testLoginSuccess() throws Exception {
        when(request.getPathInfo()).thenReturn("/login");
        when(request.getContentType()).thenReturn("application/json");
        when(request.getSession(true)).thenReturn(session);

        String json = "{\"email\":\"buyer@example.com\",\"password\":\"Password123!\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        User user = new User(1L, "Buyer One", "buyer@example.com", "hash", Role.BUYER, Timestamp.from(Instant.now()));
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(user);

        servlet.doPost(request, response);

        verify(request).changeSessionId();
        verify(session).setMaxInactiveInterval(1800);
        verify(session).setAttribute(eq("user"), eq(user));
        verify(response).setStatus(HttpServletResponse.SC_OK);

        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 on invalid credentials")
    void testLoginFailure() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getPathInfo()).thenReturn("/login");
        when(request.getContentType()).thenReturn("application/json");

        String json = "{\"email\":\"wrong@example.com\",\"password\":\"wrong\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new AuthenticationException("Invalid email or password"));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertFalse(obj.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("POST /api/auth/register returns 201 on successful registration")
    void testRegisterSuccess() throws Exception {
        when(request.getPathInfo()).thenReturn("/register");
        when(request.getContentType()).thenReturn("application/json");

        String json = "{\"name\":\"New User\",\"email\":\"new@example.com\",\"password\":\"Password123!\",\"role\":\"BUYER\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        UserResponseDTO responseDTO = new UserResponseDTO(10L, "New User", "new@example.com", Role.BUYER, Timestamp.from(Instant.now()));
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(responseDTO);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("POST /api/auth/logout invalidates session and returns 200")
    void testLogoutSuccess() throws Exception {
        when(request.getPathInfo()).thenReturn("/logout");
        when(request.getSession(false)).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).invalidate();
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
    }

    @Test
    @DisplayName("GET /api/auth/me returns 200 with profile for logged-in user")
    void testGetMeSuccess() throws Exception {
        when(request.getPathInfo()).thenReturn("/me");
        when(request.getSession(false)).thenReturn(session);

        User user = new User(1L, "Buyer One", "buyer@example.com", "hash", Role.BUYER, Timestamp.from(Instant.now()));
        when(session.getAttribute("user")).thenReturn(user);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonObject obj = JsonUtil.fromJson(responseWriter.toString(), JsonObject.class);
        assertTrue(obj.get("success").getAsBoolean());
        assertEquals("buyer@example.com", obj.getAsJsonObject("data").get("email").getAsString());
    }
}
