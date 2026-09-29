package com.mat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * AssetStock entity
 *
 * Represents the CURRENT quantity of a specific equipment type at a specific base.
 * This is the live stock ledger — it always reflects the up-to-date balance.
 *
 * One row = one (equipment, base) combination.
 * The unique constraint on (equipment_id, base_id) ensures there is never
 * more than one stock record per equipment per base.
 *
 * Examples:
 *   - equipment: "AK-47",       base: "Bangalore",  quantity: 80
 *   - equipment: "AK-47",       base: "Mangalore",  quantity: 20
 *   - equipment: "Diesel Fuel", base: "Bangalore",  quantity: 5000
 *
 * How quantity changes over time:
 *   - Purchase  → quantity at the receiving base increases
 *   - Transfer  → quantity at fromBase decreases, quantity at toBase increases
 *   - Expenditure → quantity at the base decreases
 *
 * (The logic to update quantity will be added in the next step.)
 *
 * Database table: "asset_stock"
 */
@Entity
@Table(
    name = "asset_stock",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_asset_stock_equipment_base",
            columnNames = { "equipment_id", "base_id" }
        )
    }
)
public class AssetStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The type of equipment whose stock is being tracked.
     * Many stock records can reference the same Equipment type
     * (one record per base that holds that equipment).
     */
    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    /**
     * The base at which this stock is held.
     * Many stock records can belong to the same Base
     * (one record per equipment type held at that base).
     */
    @ManyToOne
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    /**
     * The current quantity of this equipment at this base.
     * Starts at 0 and is updated whenever a Purchase, Transfer,
     * or Expenditure event occurs.
     */
    @Column(nullable = false)
    private Integer quantity;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public AssetStock() {
    }

    /** Convenience constructor for creating an AssetStock record */
    public AssetStock(Equipment equipment, Base base, Integer quantity) {
        this.equipment = equipment;
        this.base = base;
        this.quantity = quantity;
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

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "AssetStock{" +
                "id=" + id +
                ", equipment=" + (equipment != null ? equipment.getName() : "none") +
                ", base=" + (base != null ? base.getName() : "none") +
                ", quantity=" + quantity +
                '}';
    }
}
