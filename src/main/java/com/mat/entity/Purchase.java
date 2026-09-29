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
 * Purchase entity
 *
 * Represents a purchase transaction where a specific quantity of equipment
 * is procured and delivered to a military base. Each purchase record captures:
 *   - WHAT was purchased (Equipment)
 *   - WHERE it was delivered (Base)
 *   - HOW MANY units were purchased (quantity)
 *   - WHEN the purchase occurred (purchaseDate)
 *   - WHO recorded it (recordedBy → User)
 *
 * A Purchase is an "inflow" event — it adds stock to a base.
 * Later, the system will calculate Net Movement (balance) for each base by
 * combining purchases (inflows) with transfers and expenditures (outflows).
 *
 * Examples:
 *   - 500 units of "AK-47" purchased for "Base Alpha", recorded by officer "john_doe"
 *   - 10,000 liters of "Diesel Fuel" purchased for "Base Bravo", recorded by officer "jane_smith"
 *
 * Database table: "purchases"
 */
@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The equipment that was purchased.
     * Many purchases can reference the same Equipment type.
     */
    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /**
     * The base that received this purchase.
     * Many purchases can belong to the same Base.
     */
    @ManyToOne
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    /** The number of units purchased (e.g., 500 pieces, 10000 liters) */
    @Column(nullable = false)
    private Integer quantity;

    /** The date and time when this purchase was made */
    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    /**
     * The user who recorded this purchase in the system.
     * Many purchases can be recorded by the same User.
     */
    @ManyToOne
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public Purchase() {
    }

    /** Convenience constructor for creating a Purchase with all fields */
    public Purchase(Equipment equipment, Base base, Integer quantity,
                    LocalDateTime purchaseDate, User recordedBy) {
        this.equipment = equipment;
        this.base = base;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
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

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
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
        return "Purchase{" +
                "id=" + id +
                ", equipment=" + (equipment != null ? equipment.getName() : "none") +
                ", base=" + (base != null ? base.getName() : "none") +
                ", quantity=" + quantity +
                ", purchaseDate=" + purchaseDate +
                ", recordedBy=" + (recordedBy != null ? recordedBy.getUsername() : "none") +
                '}';
    }
}
