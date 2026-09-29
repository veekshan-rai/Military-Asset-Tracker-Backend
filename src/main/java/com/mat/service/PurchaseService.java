package com.mat.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Purchase;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.PurchaseRepository;

/**
 * PurchaseService
 *
 * Contains the business logic for managing Purchase records.
 * When a purchase is saved, AssetStock is automatically updated and audit log recorded.
 */
@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final EquipmentRepository equipmentRepository;
    private final BaseRepository baseRepository;
    private final AssetStockService assetStockService;
    private final AuditLogService auditLogService;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           EquipmentRepository equipmentRepository,
                           BaseRepository baseRepository,
                           AssetStockService assetStockService,
                           AuditLogService auditLogService) {
        this.purchaseRepository = purchaseRepository;
        this.equipmentRepository = equipmentRepository;
        this.baseRepository = baseRepository;
        this.assetStockService = assetStockService;
        this.auditLogService = auditLogService;
    }

    /**
     * Saves a Purchase record and updates AssetStock.
     * Both operations succeed or fail together (@Transactional).
     *
     * @param purchase the Purchase object to save
     * @return the saved Purchase (now includes full entity references and generated id)
     * @throws IllegalArgumentException if any validation rule fails
     */
    @Transactional
    public Purchase savePurchase(Purchase purchase) {
        // Validate required fields
        if (purchase.getEquipment() == null) {
            throw new IllegalArgumentException("Equipment is required.");
        }
        if (purchase.getBase() == null) {
            throw new IllegalArgumentException("Base is required.");
        }
        if (purchase.getQuantity() == null || purchase.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (purchase.getPurchaseDate() == null) {
            throw new IllegalArgumentException("Purchase date is required.");
        }

        // Fetch full Equipment entity if name is missing
        if (purchase.getEquipment().getId() != null) {
            Equipment eq = equipmentRepository.findById(purchase.getEquipment().getId())
                    .orElse(purchase.getEquipment());
            purchase.setEquipment(eq);
        }

        // Fetch full Base entity if name is missing
        if (purchase.getBase().getId() != null) {
            Base b = baseRepository.findById(purchase.getBase().getId())
                    .orElse(purchase.getBase());
            purchase.setBase(b);
        }

        // Save the purchase transaction record
        Purchase saved = purchaseRepository.save(purchase);

        // Update AssetStock: increase stock at the receiving base
        assetStockService.increaseStock(
                saved.getEquipment(),
                saved.getBase(),
                saved.getQuantity()
        );

        // Audit log
        auditLogService.log(
                "PURCHASE_CREATED",
                "Purchase",
                saved.getId(),
                saved.getRecordedBy(),
                "Purchased " + saved.getQuantity() + " " + saved.getEquipment().getName()
                        + " at " + saved.getBase().getName()
        );

        return saved;
    }

    /**
     * Retrieves all Purchase records from the database.
     */
    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    /**
     * Retrieves purchases filtered by equipment type.
     */
    public List<Purchase> getPurchasesByEquipmentType(String equipmentType) {
        return purchaseRepository.findByEquipment_EquipmentType(equipmentType);
    }

    /**
     * Retrieves purchases filtered by a specific date.
     */
    public List<Purchase> getPurchasesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return purchaseRepository.findByPurchaseDateBetween(startOfDay, endOfDay);
    }

    /**
     * Retrieves purchases filtered by base ID (used for RBAC filtering).
     */
    public List<Purchase> getPurchasesByBaseId(Long baseId) {
        return purchaseRepository.findByBase_Id(baseId);
    }
}
