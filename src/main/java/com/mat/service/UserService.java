package com.mat.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mat.entity.User;
import com.mat.repository.UserRepository;

/**
 * UserService
 *
 * Contains the business logic for managing User records.
 * Encodes passwords using BCrypt prior to saving.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Saves a User record to the database after encoding their password.
     *
     * @param user the User object to save
     * @return the saved User (with generated id)
     */
    public User saveUser(User user) {
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
