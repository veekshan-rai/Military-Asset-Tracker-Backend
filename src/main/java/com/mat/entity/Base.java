package com.mat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Base entity
 *
 * Represents a military base or installation in the system.
 * Each base has a name and a location (e.g., city or region).
 *
 * Relationships:
 *   - A Base can have many Users assigned to it (mapped from the User side).
 *   - Future entities like Purchase, Transfer, and Assignment will reference Base
 *     to track which base an operation belongs to.
 *
 * Database table: "bases"
 */
@Entity
@Table(name = "bases")
public class Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public Base() {
    }

    /** Convenience constructor for creating a Base with name and location */
    public Base(String name, String location) {
        this.name = name;
        this.location = location;
    }

    // ── Getters and Setters ───────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "Base{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
