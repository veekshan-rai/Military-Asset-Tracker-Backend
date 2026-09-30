package com.mat.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.UserRepository;

/**
 * Service for managing User records.
 * Encodes passwords using BCrypt and enforces role-based assigned base requirements.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(User user) {
        if (user.getRole() == Role.BASE_COMMANDER && user.getAssignedBase() == null) {
            throw new IllegalArgumentException("Assigned base is required for Base Commander.");
        }
        if (user.getRole() == Role.LOGISTICS_OFFICER && user.getAssignedBase() == null) {
            throw new IllegalArgumentException("Assigned base is required for Logistics Officer.");
        }
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Retrieves all User records from the database.
     *
     * @return a list of all User objects
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
