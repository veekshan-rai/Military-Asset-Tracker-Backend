package com.mat.repository;

import com.mat.entity.AssetStock;
import com.mat.entity.Base;
import com.mat.entity.Equipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for AssetStock entity.
 *
 * Provides standard CRUD operations via JpaRepository
 * and a custom finder for looking up the stock record
 * for a specific equipment at a specific base.
 */
@Repository
public interface AssetStockRepository extends JpaRepository<AssetStock, Long> {

    /**
     * Find the stock record for a given equipment at a given base.
     *
     * Since the asset_stock table has a unique constraint on (equipment_id, base_id),
     * there will be at most one result — hence Optional.
     *
     * This will be useful when updating stock after Purchase, Transfer, or Expenditure.
     */
    Optional<AssetStock> findByEquipmentAndBase(Equipment equipment, Base base);

    /**
     * Find the stock record for a given equipment ID and base ID.
     */
    Optional<AssetStock> findByEquipment_IdAndBase_Id(Long equipmentId, Long baseId);

    default Optional<AssetStock> findByEquipmentIdAndBaseId(Long equipmentId, Long baseId) {
        return findByEquipment_IdAndBase_Id(equipmentId, baseId);
    }

    /** Find all stock records for a given base */
    java.util.List<AssetStock> findByBase_Id(Long baseId);

    /** Find all stock records */
    // findAll() is inherited from JpaRepository
}
