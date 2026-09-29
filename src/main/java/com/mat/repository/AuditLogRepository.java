package com.mat.repository;

import java.util.List;

import com.mat.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AuditLogRepository
 *
 * Provides standard CRUD operations for the AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Find audit logs ordered by most recent first */
    List<AuditLog> findAllByOrderByTimestampDesc();
}
