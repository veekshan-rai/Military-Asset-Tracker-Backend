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
 * Transfer entity
 *
 * Represents the movement of equipment from one military base to another.
 * Each transfer record captures:
 *   - WHAT was transferred (Equipment)
 *   - FROM which base (fromBase)
 *   - TO which base (toBase)
 *   - HOW MANY units were transferred (quantity)
 *   - WHEN the transfer occurred (transferDate)
 *   - WHO initiated it (transferredBy → User)
 *
 * A Transfer is both an outflow for the source base and an inflow for the
 * destination base. It moves stock between bases without changing the
 * overall system total.
 *
 * Database table: "transfers"
 */
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The equipment being transferred.
     * Many transfers can reference the same Equipment type.
     */
    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /**
     * The base sending the equipment.
     * Many transfers can originate from the same Base.
     */
    @ManyToOne
    @JoinColumn(name = "from_base_id", nullable = false)
    private Base fromBase;

    /**
     * The base receiving the equipment.
     * Many transfers can be sent to the same Base.
     */
    @ManyToOne
    @JoinColumn(name = "to_base_id", nullable = false)
    private Base toBase;

    /** The number of units transferred */
    @Column(nullable = false)
    private Integer quantity;

    /** The date and time when this transfer occurred */
    @Column(nullable = false)
    private LocalDateTime transferDate;

    /**
     * The user who initiated this transfer.
     * Many transfers can be initiated by the same User.
     */
    @ManyToOne
    @JoinColumn(name = "transferred_by_id", nullable = false)
    private User transferredBy;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public Transfer() {
    }

    /** Convenience constructor for creating a Transfer with all fields */
    public Transfer(Equipment equipment, Base fromBase, Base toBase,
                    Integer quantity, LocalDateTime transferDate, User transferredBy) {
        this.equipment = equipment;
        this.fromBase = fromBase;
        this.toBase = toBase;
        this.quantity = quantity;
        this.transferDate = transferDate;
        this.transferredBy = transferredBy;
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

    public Base getFromBase() {
        return fromBase;
    }

    public void setFromBase(Base fromBase) {
        this.fromBase = fromBase;
    }

    public Base getToBase() {
        return toBase;
    }

    public void setToBase(Base toBase) {
        this.toBase = toBase;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    public User getTransferredBy() {
        return transferredBy;
    }

    public void setTransferredBy(User transferredBy) {
        this.transferredBy = transferredBy;
    }

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "Transfer{" +
                "id=" + id +
                ", equipment=" + (equipment != null ? equipment.getName() : "none") +
                ", fromBase=" + (fromBase != null ? fromBase.getName() : "none") +
                ", toBase=" + (toBase != null ? toBase.getName() : "none") +
                ", quantity=" + quantity +
                ", transferDate=" + transferDate +
                ", transferredBy=" + (transferredBy != null ? transferredBy.getUsername() : "none") +
                '}';
    }
}
