package com.mat.service;

/**
 * service package
 *
 * This package will contain Service classes that hold business logic.
 *
 * The service layer sits between the controller and the repository:
 *
 *   Request → Controller → Service → Repository → Database
 *                                          ↓
 *   Response ← Controller ← Service ←────────────
 *
 * Service classes are annotated with @Service, which:
 *   - Marks the class as a Spring-managed bean
 *   - Enables dependency injection into controllers
 *   - Enables Spring's transaction support via @Transactional
 *
 * Examples of future services:
 *   - UserService    → handles user registration, lookup, updates
 *   - AssetService   → handles asset creation, transfer, retirement
 */
// This file is intentionally left as a package marker.
// Services will be added in the next development phase.
