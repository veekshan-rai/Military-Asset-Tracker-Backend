package com.mat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.User;
import com.mat.service.UserService;

/**
 * UserController
 *
 * Handles HTTP requests for managing users.
 *
 * Endpoints:
 *   POST /api/users   → Create a new user
 *   GET  /api/users   → Retrieve all users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/users
     *
     * Creates a new User record.
     *
     * Example request body:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "secret123",
     *   "role": "LOGISTICS_OFFICER",
     *   "assignedBase": { "id": 1 }
     * }
     *
     * @param user the User data from the request body
     * @return the saved User object (with generated id)
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * GET /api/users
     *
     * Retrieves all user records from the database.
     *
     * @return a list of all User objects
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
