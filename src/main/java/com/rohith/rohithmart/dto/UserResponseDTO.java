package com.rohith.rohithmart.dto;

import com.rohith.rohithmart.model.Role;

import java.sql.Timestamp;

/**
 * Safe DTO for user representations.
 * Extends UserResponse for backward compatibility.
 */
public class UserResponseDTO extends UserResponse {

    public UserResponseDTO() {
        super();
    }

    public UserResponseDTO(Long id, String name, String email, Role role, Timestamp createdAt) {
        super(id, name, email, role, createdAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends UserResponse.Builder {
        @Override
        public Builder id(Long id) {
            super.id(id);
            return this;
        }

        @Override
        public Builder name(String name) {
            super.name(name);
            return this;
        }

        @Override
        public Builder email(String email) {
            super.email(email);
            return this;
        }

        @Override
        public Builder role(Role role) {
            super.role(role);
            return this;
        }

        @Override
        public Builder createdAt(Timestamp createdAt) {
            super.createdAt(createdAt);
            return this;
        }

        @Override
        public UserResponseDTO build() {
            UserResponse ur = super.build();
            return new UserResponseDTO(ur.getId(), ur.getName(), ur.getEmail(), ur.getRole(), ur.getCreatedAt());
        }
    }
}
