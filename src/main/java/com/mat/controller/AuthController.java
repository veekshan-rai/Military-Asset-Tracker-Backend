package com.mat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mat.dto.LoginRequest;
import com.mat.dto.LoginResponse;
import com.mat.service.AuthService;

/**
 * AuthController
 *
 * Handles authentication requests (login).
 *
 * Endpoints:
 *   POST /api/auth/login   → Authenticate a user and return a JWT token
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     *
     * Authenticates a user with username (or email) and password.
     * Returns a JWT token and basic user information on success.
     *
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
