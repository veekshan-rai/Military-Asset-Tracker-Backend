package com.mat.repository;

import com.mat.entity.Base;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BaseRepository
 *
 * Provides standard CRUD operations for the Base entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface BaseRepository extends JpaRepository<Base, Long> {
}
