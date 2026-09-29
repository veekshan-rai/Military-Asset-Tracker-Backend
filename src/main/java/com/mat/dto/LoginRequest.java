package com.mat.dto;

/**
 * LoginRequest DTO
 *
 * Used to receive login credentials from the client.
 * The client can login with either username or email.
 */
public class LoginRequest {

    private String username;
    private String password;

    // ── Constructors ──────────────────────────────────────

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ── Getters and Setters ───────────────────────────────

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
