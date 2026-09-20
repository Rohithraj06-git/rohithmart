package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dao.UserDAO;
import com.rohith.rohithmart.dto.LoginRequestDTO;
import com.rohith.rohithmart.dto.RegisterRequestDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.AuthenticationException;
import com.rohith.rohithmart.exception.ConflictException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.PasswordUtil;
import com.rohith.rohithmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementation of AuthService adhering to layered architecture and validation rules.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserDAO userDAO;

    public AuthServiceImpl() {
        this(DAOFactory.getUserDAO());
    }

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserResponseDTO register(RegisterRequestDTO request) throws AppException {
        // 1. Validation at top of service method
        if (request == null) {
            throw new ValidationException("Registration data is required");
        }
        if (!ValidationUtil.isNotEmpty(request.getName())) {
            throw new ValidationException("Full name is required");
        }
        if (!ValidationUtil.isValidEmail(request.getEmail())) {
            throw new ValidationException("A valid email address is required");
        }
        if (!ValidationUtil.hasMinLength(request.getPassword(), 8)) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        if (request.getRole() == null || request.getRole() == Role.ADMIN) {
            throw new ValidationException("Invalid role selected for self-registration");
        }

        try {
            // 2. Check existing user
            Optional<User> existing = userDAO.findByEmail(request.getEmail().trim().toLowerCase());
            if (existing.isPresent()) {
                throw new ConflictException("An account with this email already exists");
            }

            // 3. Hash password via BCrypt
            String passwordHash = PasswordUtil.hashPassword(request.getPassword());

            // 4. Create and persist user entity
            User user = new User();
            user.setName(request.getName().trim());
            user.setEmail(request.getEmail().trim().toLowerCase());
            user.setPasswordHash(passwordHash);
            user.setRole(request.getRole());

            User created = userDAO.create(user);
            logger.info("User registered successfully with ID: {}", created.getId());

            // 5. Return safe DTO without passwordHash
            return UserResponseDTO.builder()
                    .id(created.getId())
                    .name(created.getName())
                    .email(created.getEmail())
                    .role(created.getRole())
                    .createdAt(created.getCreatedAt())
                    .build();

        } catch (SQLException e) {
            logger.error("Database error during registration", e);
            throw new AppException("Failed to register user due to an internal error", e, 500);
        }
    }

    @Override
    public User login(LoginRequestDTO request) throws AppException {
        // 1. Validation at top
        if (request == null) {
            throw new ValidationException("Login credentials are required");
        }
        if (!ValidationUtil.isValidEmail(request.getEmail())) {
            throw new ValidationException("Valid email is required");
        }
        if (!ValidationUtil.isNotEmpty(request.getPassword())) {
            throw new ValidationException("Password is required");
        }

        try {
            // 2. Find by email
            Optional<User> optUser = userDAO.findByEmail(request.getEmail().trim().toLowerCase());
            if (optUser.isEmpty()) {
                throw new AuthenticationException("Invalid email or password");
            }

            User user = optUser.get();

            // 3. Verify BCrypt hash
            boolean matches = PasswordUtil.verifyPassword(request.getPassword(), user.getPasswordHash());
            if (!matches) {
                logger.warn("Failed login attempt for email: {}", request.getEmail());
                throw new AuthenticationException("Invalid email or password");
            }

            logger.info("Successful login for user ID: {}, role: {}", user.getId(), user.getRole());
            return user;

        } catch (SQLException e) {
            logger.error("Database error during login", e);
            throw new AppException("Failed to process login due to database error", e, 500);
        }
    }
}
