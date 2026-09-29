package com.mat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * User entity
 *
 * Represents a user of the Military Asset Tracker system.
 * Each user has login credentials (username, email, password),
 * a role that determines their permissions, and an assigned base.
 *
 * Relationships:
 *   - A User has one Role (stored as an enum, not a separate table).
 *   - A User is assigned to one Base (Many Users → One Base).
 *
 * Database table: "users"
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    /**
     * The user's role in the system.
     * Stored as a String in the database (e.g., "ADMIN", "BASE_COMMANDER").
     * EnumType.STRING is used instead of ORDINAL so the stored value is readable
     * and won't break if the enum order changes.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * The base this user is assigned to.
     * Many users can belong to the same base → @ManyToOne.
     * The foreign key column in the "users" table is "assigned_base_id".
     */
    @ManyToOne
    @JoinColumn(name = "assigned_base_id")
    private Base assignedBase;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public User() {
    }

    /** Convenience constructor for creating a User with all fields */
    public User(String username, String email, String password, Role role, Base assignedBase) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.assignedBase = assignedBase;
    }

    // ── Getters and Setters ───────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Base getAssignedBase() {
        return assignedBase;
    }

    public void setAssignedBase(Base assignedBase) {
        this.assignedBase = assignedBase;
    }

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", assignedBase=" + (assignedBase != null ? assignedBase.getName() : "none") +
                '}';
    }
}
