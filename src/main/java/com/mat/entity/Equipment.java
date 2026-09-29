package com.mat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Equipment entity
 *
 * Represents a type or category of military equipment that can be tracked.
 * This is a reference/catalog entity — it defines WHAT equipment exists,
 * not how much of it is at a particular base.
 *
 * Examples:
 *   - name: "AK-47",           equipmentType: "Weapon",     unit: "pieces"
 *   - name: "Diesel Fuel",     equipmentType: "Fuel",       unit: "liters"
 *   - name: "Combat Helmet",   equipmentType: "Protective", unit: "pieces"
 *   - name: "Medical Kit",     equipmentType: "Medical",    unit: "kits"
 *
 * Future entities (Purchase, Transfer, Assignment, Expenditure) will reference
 * Equipment to specify which item type is involved in the operation, along with
 * the quantity.
 *
 * Database table: "equipment"
 */
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the equipment (e.g., "AK-47", "Diesel Fuel") */
    @Column(nullable = false, unique = true)
    private String name;

    /** The category or type of equipment (e.g., "Weapon", "Fuel", "Medical") */
    @Column(nullable = false)
    private String equipmentType;

    /** The unit of measurement for this equipment (e.g., "pieces", "liters", "kits") */
    @Column(nullable = false)
    private String unit;

    // ── Constructors ──────────────────────────────────────

    /** Default no-arg constructor required by JPA */
    public Equipment() {
    }

    /** Convenience constructor for creating Equipment with all fields */
    public Equipment(String name, String equipmentType, String unit) {
        this.name = name;
        this.equipmentType = equipmentType;
        this.unit = unit;
    }

    // ── Getters and Setters ───────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // ── toString ──────────────────────────────────────────

    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", equipmentType='" + equipmentType + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
