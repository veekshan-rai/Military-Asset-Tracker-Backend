package com.mat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Expenditure;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.ExpenditureRepository;

/**
 * ExpenditureService
 *
 * Contains the business logic for managing Expenditure records.
 * When an expenditure is saved, AssetStock is automatically decreased and audit log recorded.
 *
 * Uses @Transactional so all operations succeed or fail together.
 */
@Service
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final EquipmentRepository equipmentRepository;
    private final BaseRepository baseRepository;
    private final AssetStockService assetStockService;
    private final AuditLogService auditLogService;

    public ExpenditureService(ExpenditureRepository expenditureRepository,
                               EquipmentRepository equipmentRepository,
                               BaseRepository baseRepository,
                               AssetStockService assetStockService,
                               AuditLogService auditLogService) {
        this.expenditureRepository = expenditureRepository;
        this.equipmentRepository = equipmentRepository;
        this.baseRepository = baseRepository;
        this.assetStockService = assetStockService;
        this.auditLogService = auditLogService;
    }

    /**
     * Saves an Expenditure record and decreases AssetStock.
     * Both operations succeed or fail together (@Transactional).
     *
     * Validation rules:
     *   - equipment must be provided
     *   - base must be provided
     *   - quantity must be greater than 0
     *   - expenditureDate must be provided
     *   - reason must be provided and not blank
     *   - base must have sufficient stock
     *
     * @param expenditure the Expenditure object to save
     * @return the saved Expenditure (now includes a generated id)
     * @throws IllegalArgumentException if any validation rule fails
     * @throws IllegalStateException if insufficient stock
     */
    @Transactional
    public Expenditure saveExpenditure(Expenditure expenditure) {
        if (expenditure.getEquipment() == null) {
            throw new IllegalArgumentException("Equipment is required.");
        }
        if (expenditure.getBase() == null) {
            throw new IllegalArgumentException("Base is required.");
        }
        if (expenditure.getQuantity() == null || expenditure.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (expenditure.getExpenditureDate() == null) {
            throw new IllegalArgumentException("Expenditure date is required.");
        }
        if (expenditure.getReason() == null || expenditure.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required.");
        }

        // Fetch full Equipment entity if name is missing
        if (expenditure.getEquipment().getId() != null) {
            Equipment eq = equipmentRepository.findById(expenditure.getEquipment().getId())
                    .orElse(expenditure.getEquipment());
            expenditure.setEquipment(eq);
        }

        // Fetch full Base entity if name is missing
        if (expenditure.getBase().getId() != null) {
            Base b = baseRepository.findById(expenditure.getBase().getId())
                    .orElse(expenditure.getBase());
            expenditure.setBase(b);
        }

        // Save the expenditure transaction record
        Expenditure saved = expenditureRepository.save(expenditure);

        // Update AssetStock: decrease stock at the base
        assetStockService.decreaseStock(
                saved.getEquipment(),
                saved.getBase(),
                saved.getQuantity()
        );

        // Audit log
        auditLogService.log(
                "EXPENDITURE_CREATED",
                "Expenditure",
                saved.getId(),
                saved.getRecordedBy(),
                "Expended " + saved.getQuantity() + " " + saved.getEquipment().getName()
                        + " at " + saved.getBase().getName()
                        + " — Reason: " + saved.getReason()
        );

        return saved;
    }

    /**
     * Retrieves all Expenditure records from the database.
     *
     * @return a list of all Expenditure objects
     */
    public List<Expenditure> getAllExpenditures() {
        return expenditureRepository.findAll();
    }
}
