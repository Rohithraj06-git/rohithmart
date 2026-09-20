package com.rohith.rohithmart.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a registered user entity in the database.
 * Security Note: passwordHash is never exposed in toString() or serialized DTOs.
 */
public class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private Timestamp createdAt;

    public User() {
    }

    public User(Long id, String name, String email, String passwordHash, Role role, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        // SECURITY: Sensitive credentials and password hashes are strictly masked
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='[PROTECTED]'" +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
