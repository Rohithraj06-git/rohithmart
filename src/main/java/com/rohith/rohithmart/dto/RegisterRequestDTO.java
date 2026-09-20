package com.rohith.rohithmart.dto;

import com.rohith.rohithmart.model.Role;

/**
 * Request payload for user registration.
 * Extends canonical RegisterRequest for backward compatibility.
 */
public class RegisterRequestDTO extends RegisterRequest {

    public RegisterRequestDTO() {
        super();
    }

    public RegisterRequestDTO(String name, String email, String password, Role role) {
        super(name, email, password, role);
    }
}
