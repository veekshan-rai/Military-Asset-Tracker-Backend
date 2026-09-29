package com.mat.repository;

import com.mat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository
 *
 * Provides standard CRUD operations for the User entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by username (for login) */
    Optional<User> findByUsername(String username);

    /** Find a user by email (for login) */
    Optional<User> findByEmail(String email);
}
