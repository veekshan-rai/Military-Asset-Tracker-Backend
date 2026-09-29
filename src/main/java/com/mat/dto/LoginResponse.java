package com.mat.dto;

/**
 * LoginResponse DTO
 *
 * Returned to the client after successful authentication.
 * Contains the JWT token and basic user information.
 */
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String role;
    private String assignedBaseName;
    private Long assignedBaseId;

    // ── Constructors ──────────────────────────────────────

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String username,
                         String role, String assignedBaseName, Long assignedBaseId) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.assignedBaseName = assignedBaseName;
        this.assignedBaseId = assignedBaseId;
    }

    // ── Getters and Setters ───────────────────────────────

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAssignedBaseName() {
        return assignedBaseName;
    }

    public void setAssignedBaseName(String assignedBaseName) {
        this.assignedBaseName = assignedBaseName;
    }

    public Long getAssignedBaseId() {
        return assignedBaseId;
    }

    public void setAssignedBaseId(Long assignedBaseId) {
        this.assignedBaseId = assignedBaseId;
    }
}
