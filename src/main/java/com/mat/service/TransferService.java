package com.mat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Transfer;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.TransferRepository;

/**
 * TransferService
 *
 * Contains the business logic for managing Transfer records.
 * When a transfer is saved, AssetStock is automatically updated:
 *   - fromBase stock is decreased
 *   - toBase stock is increased
 *
 * Uses @Transactional so all operations succeed or fail together.
 */
@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final EquipmentRepository equipmentRepository;
    private final BaseRepository baseRepository;
    private final AssetStockService assetStockService;
    private final AuditLogService auditLogService;

    public TransferService(TransferRepository transferRepository,
                           EquipmentRepository equipmentRepository,
                           BaseRepository baseRepository,
                           AssetStockService assetStockService,
                           AuditLogService auditLogService) {
        this.transferRepository = transferRepository;
        this.equipmentRepository = equipmentRepository;
        this.baseRepository = baseRepository;
        this.assetStockService = assetStockService;
        this.auditLogService = auditLogService;
    }

    /**
     * Saves a Transfer record and updates AssetStock at both bases.
     * Both operations succeed or fail together (@Transactional).
     *
     * Validation rules:
     *   - equipment must be provided
     *   - fromBase must be provided
     *   - toBase must be provided
     *   - fromBase and toBase must not be the same
     *   - quantity must be greater than 0
     *   - transferDate must be provided
     *   - fromBase must have sufficient stock
     *
     * @param transfer the Transfer object to save
     * @return the saved Transfer (now includes a generated id)
     * @throws IllegalArgumentException if any validation rule fails
     * @throws IllegalStateException if insufficient stock at fromBase
     */
    @Transactional
    public Transfer saveTransfer(Transfer transfer) {
        if (transfer.getEquipment() == null) {
            throw new IllegalArgumentException("Equipment is required.");
        }
        if (transfer.getFromBase() == null) {
            throw new IllegalArgumentException("Source base (fromBase) is required.");
        }
        if (transfer.getToBase() == null) {
            throw new IllegalArgumentException("Destination base (toBase) is required.");
        }
        if (transfer.getFromBase().getId() != null
                && transfer.getToBase().getId() != null
                && transfer.getFromBase().getId().equals(transfer.getToBase().getId())) {
            throw new IllegalArgumentException("Source base and destination base must not be the same.");
        }
        if (transfer.getQuantity() == null || transfer.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (transfer.getTransferDate() == null) {
            throw new IllegalArgumentException("Transfer date is required.");
        }

        // Fetch full Equipment entity if name is missing
        if (transfer.getEquipment().getId() != null) {
            Equipment eq = equipmentRepository.findById(transfer.getEquipment().getId())
                    .orElse(transfer.getEquipment());
            transfer.setEquipment(eq);
        }

        // Fetch full fromBase entity if name is missing
        if (transfer.getFromBase().getId() != null) {
            Base fb = baseRepository.findById(transfer.getFromBase().getId())
                    .orElse(transfer.getFromBase());
            transfer.setFromBase(fb);
        }

        // Fetch full toBase entity if name is missing
        if (transfer.getToBase().getId() != null) {
            Base tb = baseRepository.findById(transfer.getToBase().getId())
                    .orElse(transfer.getToBase());
            transfer.setToBase(tb);
        }

        // Save the transfer transaction record
        Transfer saved = transferRepository.save(transfer);

        // Update AssetStock: decrease stock at source base
        assetStockService.decreaseStock(
                saved.getEquipment(),
                saved.getFromBase(),
                saved.getQuantity()
        );

        // Update AssetStock: increase stock at destination base
        assetStockService.increaseStock(
                saved.getEquipment(),
                saved.getToBase(),
                saved.getQuantity()
        );

        // Audit log
        auditLogService.log(
                "TRANSFER_CREATED",
                "Transfer",
                saved.getId(),
                saved.getTransferredBy(),
                "Transferred " + saved.getQuantity() + " " + saved.getEquipment().getName()
                        + " from " + saved.getFromBase().getName()
                        + " to " + saved.getToBase().getName()
        );

        return saved;
    }

    /**
     * Retrieves all Transfer records from the database.
     *
     * @return a list of all Transfer objects
     */
    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }
}
