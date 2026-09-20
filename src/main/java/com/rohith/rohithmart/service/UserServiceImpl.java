package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dao.UserDAO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.AuthenticationException;
import com.rohith.rohithmart.exception.ConflictException;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.PasswordUtil;
import com.rohith.rohithmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService with profile management, validation, and security controls.
 */
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;

    public UserServiceImpl() {
        this(DAOFactory.getUserDAO());
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserResponseDTO getUserById(Long userId) throws AppException {
        ValidationUtil.validateId(userId, "userId");

        try {
            Optional<User> opt = userDAO.findById(userId);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("User not found with ID: " + userId);
            }
            return toDTO(opt.get());
        } catch (SQLException e) {
            logger.error("Error retrieving user ID {}", userId, e);
            throw new AppException("Failed to load user profile", e, 500);
        }
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) throws AppException {
        String validatedEmail = ValidationUtil.validateEmail(email);

        try {
            Optional<User> opt = userDAO.findByEmail(validatedEmail);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("User not found with email: " + validatedEmail);
            }
            return toDTO(opt.get());
        } catch (SQLException e) {
            logger.error("Error retrieving user email {}", validatedEmail, e);
            throw new AppException("Failed to load user profile", e, 500);
        }
    }

    @Override
    public UserResponseDTO updateProfile(Long userId, String name, String email) throws AppException {
        ValidationUtil.validateId(userId, "userId");
        String validatedName = ValidationUtil.validateNotBlank(name, "name");
        String validatedEmail = ValidationUtil.validateEmail(email);

        try {
            Optional<User> opt = userDAO.findById(userId);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("User not found with ID: " + userId);
            }
            User user = opt.get();

            // Check if email changed and is taken by someone else
            if (!user.getEmail().equalsIgnoreCase(validatedEmail)) {
                Optional<User> existing = userDAO.findByEmail(validatedEmail);
                if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                    throw new ConflictException("Email address is already in use by another account");
                }
            }

            user.setName(validatedName);
            user.setEmail(validatedEmail);
            userDAO.update(user);

            logger.info("Profile updated for user ID: {}", userId);
            return toDTO(user);
        } catch (SQLException e) {
            logger.error("Error updating profile for user ID {}", userId, e);
            throw new AppException("Failed to update profile", e, 500);
        }
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) throws AppException {
        ValidationUtil.validateId(userId, "userId");
        ValidationUtil.validateNotBlank(oldPassword, "oldPassword");
        ValidationUtil.validatePassword(newPassword);

        try {
            Optional<User> opt = userDAO.findById(userId);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("User not found with ID: " + userId);
            }
            User user = opt.get();

            if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Current password does not match");
            }

            String newHash = PasswordUtil.hashPassword(newPassword);
            user.setPasswordHash(newHash);
            userDAO.update(user);

            logger.info("Password changed successfully for user ID: {}", userId);
        } catch (SQLException e) {
            logger.error("Error changing password for user ID {}", userId, e);
            throw new AppException("Failed to change password", e, 500);
        }
    }

    @Override
    public UserResponseDTO updateUserRole(Long adminUserId, Long targetUserId, Role newRole) throws AppException {
        ValidationUtil.validateId(adminUserId, "adminUserId");
        ValidationUtil.validateId(targetUserId, "targetUserId");
        ValidationUtil.validateNotNull(newRole, "newRole");

        try {
            Optional<User> adminOpt = userDAO.findById(adminUserId);
            if (adminOpt.isEmpty() || adminOpt.get().getRole() != Role.ADMIN) {
                throw new ForbiddenException("Only administrators can update user roles");
            }

            Optional<User> targetOpt = userDAO.findById(targetUserId);
            if (targetOpt.isEmpty()) {
                throw new ResourceNotFoundException("Target user not found with ID: " + targetUserId);
            }

            User target = targetOpt.get();
            target.setRole(newRole);
            userDAO.update(target);

            logger.info("User ID {} role updated to {} by Admin ID {}", targetUserId, newRole, adminUserId);
            return toDTO(target);
        } catch (SQLException e) {
            logger.error("Error updating user role for user ID {}", targetUserId, e);
            throw new AppException("Failed to update user role", e, 500);
        }
    }

    @Override
    public List<UserResponseDTO> getAllUsers() throws AppException {
        try {
            List<User> list = userDAO.findAll();
            List<UserResponseDTO> dtos = new ArrayList<>(list.size());
            for (User u : list) {
                dtos.add(toDTO(u));
            }
            return dtos;
        } catch (SQLException e) {
            logger.error("Error retrieving all users", e);
            throw new AppException("Failed to load user list", e, 500);
        }
    }

    @Override
    public int getTotalUserCount() throws AppException {
        try {
            return userDAO.countAll();
        } catch (SQLException e) {
            logger.error("Error retrieving user count", e);
            throw new AppException("Failed to count users", e, 500);
        }
    }

    private UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
