package com.mat.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mat.entity.Base;
import com.mat.entity.Purchase;

/**
 * PurchaseRepository
 *
 * Provides standard CRUD operations for the Purchase entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /** Filter purchases by equipment type */
    List<Purchase> findByEquipment_EquipmentType(String equipmentType);

    /** Filter purchases by date range */
    List<Purchase> findByPurchaseDateBetween(LocalDateTime start, LocalDateTime end);

    /** Filter purchases by base */
    List<Purchase> findByBase(Base base);

    /** Filter purchases by base and date range */
    List<Purchase> findByBaseAndPurchaseDateBetween(Base base, LocalDateTime start, LocalDateTime end);

    /** Filter purchases by base ID */
    List<Purchase> findByBase_Id(Long baseId);
}
