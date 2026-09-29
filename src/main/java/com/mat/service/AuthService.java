package com.mat.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mat.dto.LoginRequest;
import com.mat.dto.LoginResponse;
import com.mat.entity.User;
import com.mat.repository.UserRepository;
import com.mat.security.JwtService;

/**
 * AuthService
 *
 * Handles authentication (login) logic.
 * Supports login by username or email.
 * Uses BCrypt for password verification.
 * Handles transition from plain-text passwords to BCrypt.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticate a user and return a JWT token with user info.
     *
     * Supports login by username or email.
     * Handles transition: if the stored password is plain-text (not BCrypt-encoded),
     * it checks using plain-text comparison and then upgrades to BCrypt.
     *
     * @param request the login request with username and password
     * @return LoginResponse with JWT token and user details
     * @throws IllegalArgumentException if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email is required.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        // Try to find user by username first, then by email
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid username or password.")));

        // Check password — handle both BCrypt and plain-text (transition)
        boolean passwordMatch;
        if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$")) {
            // Password is already BCrypt-encoded
            passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } else {
            // Password is plain-text (legacy) — compare directly
            passwordMatch = user.getPassword().equals(request.getPassword());
            if (passwordMatch) {
                // Upgrade to BCrypt for future logins
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }
        }

        if (!passwordMatch) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId()
        );

        // Build response
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getAssignedBase() != null ? user.getAssignedBase().getName() : null,
                user.getAssignedBase() != null ? user.getAssignedBase().getId() : null
        );
    }
}
