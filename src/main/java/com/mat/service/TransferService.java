package com.mat.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Role;
import com.mat.entity.Transfer;
import com.mat.entity.User;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.TransferRepository;
import com.mat.repository.UserRepository;
import com.mat.security.SecurityUtils;

/**
 * TransferService
 *
 * Contains the business logic for managing Transfer records.
 * When a transfer is saved, the authenticated user is automatically set as transferredBy,
 * and AssetStock is automatically updated:
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
    private final UserRepository userRepository;

    public TransferService(TransferRepository transferRepository,
                           EquipmentRepository equipmentRepository,
                           BaseRepository baseRepository,
                           AssetStockService assetStockService,
                           AuditLogService auditLogService,
                           UserRepository userRepository) {
        this.transferRepository = transferRepository;
        this.equipmentRepository = equipmentRepository;
        this.baseRepository = baseRepository;
        this.assetStockService = assetStockService;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
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

        // Automatically assign authenticated user from SecurityContext
        User authenticatedUser = getAuthenticatedUser();
        transfer.setTransferredBy(authenticatedUser);

        // Enforce base-scope validation for BASE_COMMANDER (can only transfer FROM their assigned base)
        if (authenticatedUser.getRole() == Role.BASE_COMMANDER || SecurityUtils.isBaseCommander()) {
            Base assignedBase = authenticatedUser.getAssignedBase();
            if (assignedBase == null || assignedBase.getId() == null
                    || !assignedBase.getId().equals(transfer.getFromBase().getId())) {
                throw new AccessDeniedException(
                        "Access denied: Base Commander can only transfer assets from their assigned base."
                );
            }
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

    private User getAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                return user;
            }
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return user;
            }
        }
        throw new IllegalStateException("Authentication required: No authenticated user found in security context.");
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
