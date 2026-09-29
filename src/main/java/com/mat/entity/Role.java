package com.mat.entity;

/**
 * Role enum
 *
 * Defines the three roles required by the Military Asset Tracker system.
 *
 * - ADMIN              → Full system access. Can manage users, bases, and all assets.
 * - BASE_COMMANDER     → Commands a specific base. Can oversee assets at their assigned base.
 * - LOGISTICS_OFFICER  → Handles day-to-day asset operations (purchases, transfers, etc.)
 *
 * Stored in the database as a String (e.g., "ADMIN") using @Enumerated(EnumType.STRING)
 * on the User entity's role field.
 */
public enum Role {
    ADMIN,
    BASE_COMMANDER,
    LOGISTICS_OFFICER
}
