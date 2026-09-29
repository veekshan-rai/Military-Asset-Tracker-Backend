package com.mat.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.mat.entity.Assignment;
import com.mat.entity.Base;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AssignmentRepository
 *
 * Provides standard CRUD operations for the Assignment entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** Filter assignments by base */
    List<Assignment> findByBase(Base base);

    /** Filter assignments by base and date range */
    List<Assignment> findByBaseAndAssignmentDateBetween(Base base, LocalDateTime start, LocalDateTime end);

    /** Filter assignments by base ID */
    List<Assignment> findByBase_Id(Long baseId);
}
