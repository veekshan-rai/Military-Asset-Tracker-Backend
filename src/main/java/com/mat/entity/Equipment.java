package com.mat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Equipment entity representing a catalog item/category tracked in the MAT system.
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
