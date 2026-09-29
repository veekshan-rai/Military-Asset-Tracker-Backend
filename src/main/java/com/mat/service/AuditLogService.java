package com.mat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mat.entity.AuditLog;
import com.mat.entity.User;
import com.mat.repository.AuditLogRepository;

/**
 * AuditLogService
 *
 * Manages audit log entries. Provides methods to record actions
 * and retrieve audit history.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records an audit log entry.
     *
     * @param action      the action name (e.g., "PURCHASE_CREATED")
     * @param entityType  the type of entity (e.g., "Purchase")
     * @param entityId    the ID of the entity
     * @param performedBy the user who performed the action (can be null for system actions)
     * @param description a human-readable description
     */
    public void log(String action, String entityType, Long entityId,
                    User performedBy, String description) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, performedBy, description);
        auditLogRepository.save(auditLog);
    }

    /**
     * Retrieves all audit log entries, most recent first.
     */
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
