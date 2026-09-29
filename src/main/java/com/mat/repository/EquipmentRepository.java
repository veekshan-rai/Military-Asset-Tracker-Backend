package com.mat.repository;

import com.mat.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * EquipmentRepository
 *
 * Provides standard CRUD operations for the Equipment entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
