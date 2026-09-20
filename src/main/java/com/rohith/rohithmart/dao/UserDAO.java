package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for user operations.
 */
public interface UserDAO {

    /**
     * Finds a user by unique identifier.
     *
     * @param id the user ID
     * @return Optional containing user if found
     * @throws SQLException on database failure
     */
    Optional<User> findById(Long id) throws SQLException;

    /**
     * Finds a user by email address.
     *
     * @param email the user email
     * @return Optional containing user if found
     * @throws SQLException on database failure
     */
    Optional<User> findByEmail(String email) throws SQLException;

    /**
     * Persists a new user record.
     *
     * @param user the user to create
     * @return created user with generated ID
     * @throws SQLException on database failure
     */
    User create(User user) throws SQLException;

    /**
     * Updates an existing user's profile and credentials.
     *
     * @param user user entity with updated information
     * @return true if updated successfully, false otherwise
     * @throws SQLException on database failure
     */
    boolean update(User user) throws SQLException;

    /**
     * Retrieves all registered users in the system.
     *
     * @return list of all users
     * @throws SQLException on database failure
     */
    List<User> findAll() throws SQLException;

    /**
     * Counts total number of registered users.
     *
     * @return count of users
     * @throws SQLException on database failure
     */
    int countAll() throws SQLException;
}
