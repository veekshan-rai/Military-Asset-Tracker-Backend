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
 * AssetStock entity representing the current quantity of an equipment type at a base.
 * Unique constraint on (equipment_id, base_id) ensures at most one record per item per base.
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
