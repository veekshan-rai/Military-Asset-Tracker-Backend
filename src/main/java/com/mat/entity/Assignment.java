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
 * Assignment entity
 *
 * Represents the assignment of equipment to specific personnel at a base.
 * Each assignment record captures:
 *   - WHAT was assigned (Equipment)
 *   - WHERE the assignment happened (Base)
 *   - TO WHOM it was assigned (personnelName)
 *   - HOW MANY units were assigned (quantity)
 *   - WHEN the assignment occurred (assignmentDate)
 *   - WHO recorded it (assignedBy → User)
 *
 * An Assignment is a usage/allocation record. It tracks which personnel
 * received equipment, but does NOT automatically reduce stock balance.
 *
 * Database table: "assignments"
 */
@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The equipment being assigned.
     * Many assignments can reference the same Equipment type.
     */
    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /**
     * The base where this assignment takes place.
     * Many assignments can belong to the same Base.
     */
    @ManyToOne
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    /** The name of the person receiving the equipment (e.g., "Sgt. Kumar") */
    @Column(nullable = false)
    private String personnelName;

    /** The number of units assigned */
    @Column(nullable = false)
    private Integer quantity;

    /** The date and time when this assignment was made */
    @Column(nullable = false)
    private LocalDateTime assignmentDate;

    /**
     * The user who recorded this assignment in the system.
     * Many assignments can be recorded by the same User.
     */
    @ManyToOne
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public Assignment() {
    }

    /** Convenience constructor for creating an Assignment with all fields */
    public Assignment(Equipment equipment, Base base, String personnelName,
                      Integer quantity, LocalDateTime assignmentDate, User assignedBy) {
        this.equipment = equipment;
        this.base = base;
        this.personnelName = personnelName;
        this.quantity = quantity;
        this.assignmentDate = assignmentDate;
        this.assignedBy = assignedBy;
    }

    // ── Getters and Setters ───────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public String getPersonnelName() {
        return personnelName;
    }

    public void setPersonnelName(String personnelName) {
        this.personnelName = personnelName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDateTime assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", equipment=" + (equipment != null ? equipment.getName() : "none") +
                ", base=" + (base != null ? base.getName() : "none") +
                ", personnelName='" + personnelName + '\'' +
                ", quantity=" + quantity +
                ", assignmentDate=" + assignmentDate +
                ", assignedBy=" + (assignedBy != null ? assignedBy.getUsername() : "none") +
                '}';
    }
}
