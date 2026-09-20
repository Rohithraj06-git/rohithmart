package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dto.LoginRequestDTO;
import com.rohith.rohithmart.dto.RegisterRequestDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.model.User;

/**
 * Service interface for authentication and user account management.
 */
public interface AuthService {

    /**
     * Registers a new user account after validating input constraints.
     *
     * @param request registration parameters
     * @return created user DTO without sensitive credentials
     * @throws AppException if validation fails or email already registered
     */
    UserResponseDTO register(RegisterRequestDTO request) throws AppException;

    /**
     * Authenticates user credentials.
     *
     * @param request login parameters
     * @return authenticated User entity for session binding
     * @throws AppException if credentials are invalid
     */
    User login(LoginRequestDTO request) throws AppException;
}
