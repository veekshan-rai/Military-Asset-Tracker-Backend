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
 * Expenditure entity representing consumption, loss, or usage of equipment at a base.
 */
@Entity
@Table(name = "expenditures")
public class Expenditure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The equipment that was expended.
     * Many expenditures can reference the same Equipment type.
     */
    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /**
     * The base where this expenditure occurred.
     * Many expenditures can belong to the same Base.
     */
    @ManyToOne
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    /** The number of units expended */
    @Column(nullable = false)
    private Integer quantity;

    /** The date and time when this expenditure occurred */
    @Column(nullable = false)
    private LocalDateTime expenditureDate;

    /** The reason for the expenditure (e.g., "Training exercise", "Combat loss", "Expired") */
    @Column(nullable = false)
    private String reason;

    /**
     * The user who recorded this expenditure in the system.
     * Many expenditures can be recorded by the same User.
     */
    @ManyToOne
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public Expenditure() {
    }

    /** Convenience constructor for creating an Expenditure with all fields */
    public Expenditure(Equipment equipment, Base base, Integer quantity,
                       LocalDateTime expenditureDate, String reason, User recordedBy) {
        this.equipment = equipment;
        this.base = base;
        this.quantity = quantity;
        this.expenditureDate = expenditureDate;
        this.reason = reason;
        this.recordedBy = recordedBy;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getExpenditureDate() {
        return expenditureDate;
    }

    public void setExpenditureDate(LocalDateTime expenditureDate) {
        this.expenditureDate = expenditureDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "Expenditure{" +
                "id=" + id +
                ", equipment=" + (equipment != null ? equipment.getName() : "none") +
                ", base=" + (base != null ? base.getName() : "none") +
                ", quantity=" + quantity +
                ", expenditureDate=" + expenditureDate +
                ", reason='" + reason + '\'' +
                ", recordedBy=" + (recordedBy != null ? recordedBy.getUsername() : "none") +
                '}';
    }
}
