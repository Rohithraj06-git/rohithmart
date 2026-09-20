package com.rohith.rohithmart.dto;

/**
 * Request payload for user sign in.
 * Extends canonical LoginRequest for backward compatibility.
 */
public class LoginRequestDTO extends LoginRequest {

    public LoginRequestDTO() {
        super();
    }

    public LoginRequestDTO(String email, String password) {
        super(email, password);
    }
}
