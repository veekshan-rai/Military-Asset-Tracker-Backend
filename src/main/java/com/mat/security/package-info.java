package com.mat.security;

/**
 * security package
 *
 * This package will contain all custom Spring Security components.
 *
 * Planned components:
 *
 *   JwtUtil.java
 *     - Utility class for generating and validating JWT tokens
 *     - Used when a user logs in to produce a signed token
 *     - Used on every request to verify the token is valid and not expired
 *
 *   JwtAuthenticationFilter.java
 *     - A custom filter that intercepts every HTTP request
 *     - Reads the Authorization header (Bearer <token>)
 *     - Validates the token and sets the authenticated user in the
 *       Spring Security context so the rest of the request is secured
 *
 *   UserDetailsServiceImpl.java
 *     - Implements Spring Security's UserDetailsService interface
 *     - Loads a User from the database by username for authentication
 */
// This file is intentionally left as a package marker.
// Security components will be added in the authentication phase.
