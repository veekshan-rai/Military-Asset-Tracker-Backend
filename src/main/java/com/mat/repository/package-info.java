package com.mat.repository;

/**
 * repository package
 *
 * This package will contain Spring Data JPA Repository interfaces.
 * Repositories handle all database CRUD operations.
 *
 * Each repository interface extends JpaRepository<EntityType, IdType>,
 * which automatically provides methods like:
 *   - findAll()
 *   - findById(id)
 *   - save(entity)
 *   - deleteById(id)
 *
 * You can also define custom query methods using method naming conventions
 * or @Query annotations.
 *
 * Examples of future repositories:
 *   - UserRepository    extends JpaRepository<User, Long>
 *   - AssetRepository   extends JpaRepository<Asset, Long>
 */
// This file is intentionally left as a package marker.
// Repositories will be added in the next development phase.
