package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.UserDAO;
import com.rohith.rohithmart.dto.LoginRequestDTO;
import com.rohith.rohithmart.dto.RegisterRequestDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AuthenticationException;
import com.rohith.rohithmart.exception.ConflictException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userDAO);
    }

    @Test
    @DisplayName("Register successful with valid input")
    void testRegisterSuccess() throws Exception {
        RegisterRequestDTO req = new RegisterRequestDTO("Alice", "alice@example.com", "Password@123", Role.BUYER);

        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userDAO.create(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponseDTO result = authService.register(req);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals(Role.BUYER, result.getRole());
        verify(userDAO, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Register fails if email is already taken")
    void testRegisterDuplicateEmail() throws SQLException {
        RegisterRequestDTO req = new RegisterRequestDTO("Bob", "bob@example.com", "Password@123", Role.BUYER);
        when(userDAO.findByEmail("bob@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("Register fails on invalid short password")
    void testRegisterShortPassword() {
        RegisterRequestDTO req = new RegisterRequestDTO("Charlie", "charlie@example.com", "short", Role.BUYER);
        assertThrows(ValidationException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("Login successful with correct credentials")
    void testLoginSuccess() throws Exception {
        String rawPassword = "ValidPassword123";
        String hash = PasswordUtil.hashPassword(rawPassword);
        User mockUser = new User(1L, "David", "david@example.com", hash, Role.BUYER, null);

        when(userDAO.findByEmail("david@example.com")).thenReturn(Optional.of(mockUser));

        LoginRequestDTO req = new LoginRequestDTO("david@example.com", rawPassword);
        User result = authService.login(req);

        assertNotNull(result);
        assertEquals("david@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Login fails with invalid password")
    void testLoginInvalidPassword() throws SQLException {
        String hash = PasswordUtil.hashPassword("CorrectPassword123");
        User mockUser = new User(1L, "David", "david@example.com", hash, Role.BUYER, null);

        when(userDAO.findByEmail("david@example.com")).thenReturn(Optional.of(mockUser));

        LoginRequestDTO req = new LoginRequestDTO("david@example.com", "WrongPassword");
        assertThrows(AuthenticationException.class, () -> authService.login(req));
    }
}
