package com.mat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * AuditLog entity
 *
 * Records important system actions for accountability and tracking.
 * Each audit log entry captures WHO did WHAT, WHEN, and to WHICH entity.
 *
 * Examples:
 *   - action: "PURCHASE_CREATED", entityType: "Purchase", entityId: 5
 *   - action: "TRANSFER_CREATED", entityType: "Transfer", entityId: 12
 *   - action: "LOGIN", entityType: "User", entityId: 3
 *
 * Database table: "audit_logs"
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The action performed (e.g., "PURCHASE_CREATED", "TRANSFER_CREATED", "LOGIN") */
    @Column(nullable = false)
    private String action;

    /** The type of entity affected (e.g., "Purchase", "Transfer", "User") */
    @Column(nullable = false)
    private String entityType;

    /** The ID of the entity affected */
    private Long entityId;

    /** The user who performed the action */
    @ManyToOne
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    /** When the action occurred */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** A human-readable description of what happened */
    @Column(length = 500)
    private String description;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public AuditLog() {
    }

    /** Convenience constructor */
    public AuditLog(String action, String entityType, Long entityId,
                    User performedBy, String description) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    // ── Getters and Setters ───────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", performedBy=" + (performedBy != null ? performedBy.getUsername() : "system") +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}
