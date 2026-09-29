package com.mat.dto;

/**
 * dto package (Data Transfer Objects)
 *
 * This package will contain DTO classes used to transfer data
 * between the backend and the React frontend (or any API client).
 *
 * Why use DTOs instead of exposing entities directly?
 *   - Security: You control exactly which fields are exposed via the API.
 *     For example, you would never send a password hash in a UserDTO.
 *   - Flexibility: The API contract can differ from the database schema.
 *   - Validation: DTOs carry @Valid annotations used when receiving
 *     request data from the frontend.
 *
 * Examples of future DTOs:
 *   - LoginRequest        → { username, password }
 *   - AuthResponse        → { token, role, username }
 *   - AssetRequestDTO     → fields for creating/updating an asset
 *   - AssetResponseDTO    → fields returned to the frontend
 */
// This file is intentionally left as a package marker.
// DTOs will be added in the next development phase.
