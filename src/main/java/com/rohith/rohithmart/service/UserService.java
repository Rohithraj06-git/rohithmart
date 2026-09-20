package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.model.Role;

import java.util.List;

/**
 * Service interface for user profile management, administration, and account settings.
 */
public interface UserService {

    /**
     * Retrieves user profile details by user ID.
     *
     * @param userId user ID
     * @return UserResponseDTO without sensitive password hashes
     * @throws AppException if user not found
     */
    UserResponseDTO getUserById(Long userId) throws AppException;

    /**
     * Retrieves user profile details by email address.
     *
     * @param email user email
     * @return UserResponseDTO
     * @throws AppException if user not found
     */
    UserResponseDTO getUserByEmail(String email) throws AppException;

    /**
     * Updates an existing user's profile information (name and email).
     *
     * @param userId user ID
     * @param name new full name
     * @param email new email address
     * @return updated UserResponseDTO
     * @throws AppException if validation fails or email already taken
     */
    UserResponseDTO updateProfile(Long userId, String name, String email) throws AppException;

    /**
     * Changes a user's password after verifying their old password.
     *
     * @param userId user ID
     * @param oldPassword current password
     * @param newPassword new password
     * @throws AppException if current password incorrect or new password invalid
     */
    void changePassword(Long userId, String oldPassword, String newPassword) throws AppException;

    /**
     * Updates a user's role (Admin functionality).
     *
     * @param adminUserId ID of administrator performing the action
     * @param targetUserId ID of user to modify
     * @param newRole new role to assign
     * @return updated UserResponseDTO
     * @throws AppException if unauthorized or user not found
     */
    UserResponseDTO updateUserRole(Long adminUserId, Long targetUserId, Role newRole) throws AppException;

    /**
     * Retrieves all users on the platform (Admin oversight).
     *
     * @return list of UserResponseDTOs
     * @throws AppException on failure
     */
    List<UserResponseDTO> getAllUsers() throws AppException;

    /**
     * Returns total count of registered users.
     *
     * @return total user count
     * @throws AppException on failure
     */
    int getTotalUserCount() throws AppException;
}
