package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.UserDAO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AuthenticationException;
import com.rohith.rohithmart.exception.ConflictException;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests (Mockito)")
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDAO);
    }

    @Test
    @DisplayName("getUserById: success when user exists")
    void testGetUserByIdSuccess() throws Exception {
        User user = new User(1L, "Alice", "alice@example.com", "hash", Role.BUYER, Timestamp.from(Instant.now()));
        when(userDAO.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO dto = userService.getUserById(1L);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Alice", dto.getName());
        assertEquals("alice@example.com", dto.getEmail());
    }

    @Test
    @DisplayName("getUserById: throws ResourceNotFoundException when user does not exist")
    void testGetUserByIdNotFound() throws Exception {
        when(userDAO.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("getUserById: throws ValidationException on invalid ID")
    void testGetUserByIdInvalidId() {
        assertThrows(ValidationException.class, () -> userService.getUserById(0L));
        assertThrows(ValidationException.class, () -> userService.getUserById(null));
    }

    @Test
    @DisplayName("updateProfile: success when valid and email not taken")
    void testUpdateProfileSuccess() throws Exception {
        User user = new User(1L, "Old Name", "old@example.com", "hash", Role.BUYER, Timestamp.from(Instant.now()));
        when(userDAO.findById(1L)).thenReturn(Optional.of(user));
        when(userDAO.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userDAO.update(any(User.class))).thenReturn(true);

        UserResponseDTO updated = userService.updateProfile(1L, "New Name", "new@example.com");
        assertEquals("New Name", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        verify(userDAO).update(user);
    }

    @Test
    @DisplayName("updateProfile: throws ConflictException when new email is already in use")
    void testUpdateProfileEmailTaken() throws Exception {
        User user = new User(1L, "Alice", "alice@example.com", "hash", Role.BUYER, Timestamp.from(Instant.now()));
        User existingOther = new User(2L, "Bob", "bob@example.com", "hash2", Role.BUYER, Timestamp.from(Instant.now()));

        when(userDAO.findById(1L)).thenReturn(Optional.of(user));
        when(userDAO.findByEmail("bob@example.com")).thenReturn(Optional.of(existingOther));

        assertThrows(ConflictException.class, () -> userService.updateProfile(1L, "Alice", "bob@example.com"));
        verify(userDAO, never()).update(any());
    }

    @Test
    @DisplayName("changePassword: success with correct old password and valid new password")
    void testChangePasswordSuccess() throws Exception {
        String currentRaw = "OldPassword@123";
        String currentHash = PasswordUtil.hashPassword(currentRaw);
        User user = new User(1L, "Alice", "alice@example.com", currentHash, Role.BUYER, Timestamp.from(Instant.now()));

        when(userDAO.findById(1L)).thenReturn(Optional.of(user));
        when(userDAO.update(any(User.class))).thenReturn(true);

        userService.changePassword(1L, currentRaw, "NewSecurePassword@999");
        verify(userDAO).update(user);
    }

    @Test
    @DisplayName("changePassword: fails when old password is incorrect")
    void testChangePasswordWrongOldPassword() throws Exception {
        String currentHash = PasswordUtil.hashPassword("CorrectPass@123");
        User user = new User(1L, "Alice", "alice@example.com", currentHash, Role.BUYER, Timestamp.from(Instant.now()));

        when(userDAO.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class,
                () -> userService.changePassword(1L, "WrongPass@123", "NewSecurePassword@999"));
        verify(userDAO, never()).update(any());
    }

    @Test
    @DisplayName("updateUserRole: admin can update role")
    void testUpdateUserRoleByAdmin() throws Exception {
        User admin = new User(1L, "Admin", "admin@example.com", "hash", Role.ADMIN, Timestamp.from(Instant.now()));
        User target = new User(2L, "Buyer", "buyer@example.com", "hash", Role.BUYER, Timestamp.from(Instant.now()));

        when(userDAO.findById(1L)).thenReturn(Optional.of(admin));
        when(userDAO.findById(2L)).thenReturn(Optional.of(target));
        when(userDAO.update(any(User.class))).thenReturn(true);

        UserResponseDTO result = userService.updateUserRole(1L, 2L, Role.SELLER);
        assertEquals(Role.SELLER, result.getRole());
        verify(userDAO).update(target);
    }

    @Test
    @DisplayName("updateUserRole: non-admin throws ForbiddenException")
    void testUpdateUserRoleUnauthorized() throws Exception {
        User regularUser = new User(1L, "Regular", "reg@example.com", "hash", Role.BUYER, Timestamp.from(Instant.now()));
        when(userDAO.findById(1L)).thenReturn(Optional.of(regularUser));

        assertThrows(ForbiddenException.class, () -> userService.updateUserRole(1L, 2L, Role.SELLER));
        verify(userDAO, never()).update(any());
    }

    @Test
    @DisplayName("getAllUsers and getTotalUserCount")
    void testGetAllUsersAndCount() throws Exception {
        User u1 = new User(1L, "A", "a@example.com", "h1", Role.BUYER, null);
        User u2 = new User(2L, "B", "b@example.com", "h2", Role.SELLER, null);
        when(userDAO.findAll()).thenReturn(List.of(u1, u2));
        when(userDAO.countAll()).thenReturn(2);

        List<UserResponseDTO> all = userService.getAllUsers();
        assertEquals(2, all.size());
        assertEquals(2, userService.getTotalUserCount());
    }
}
