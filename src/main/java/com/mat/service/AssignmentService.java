package com.mat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mat.entity.AssetStock;
import com.mat.entity.Assignment;
import com.mat.entity.Base;
import com.mat.entity.Equipment;
import com.mat.entity.Role;
import com.mat.entity.User;
import com.mat.repository.AssetStockRepository;
import com.mat.repository.AssignmentRepository;
import com.mat.repository.BaseRepository;
import com.mat.repository.EquipmentRepository;
import com.mat.repository.UserRepository;
import com.mat.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;

/**
 * AssignmentService
 *
 * Contains the business logic for managing Assignment records.
 * Acts as the middle layer between the Controller and the Repository.
 *
 * Provides:
 *   - Saving an assignment with validation, auto-assigned user, and stock validation
 *   - Retrieving all assignments
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final BaseRepository baseRepository;
    private final UserRepository userRepository;
    private final AssetStockRepository assetStockRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             EquipmentRepository equipmentRepository,
                             BaseRepository baseRepository,
                             UserRepository userRepository,
                             AssetStockRepository assetStockRepository) {
        this.assignmentRepository = assignmentRepository;
        this.equipmentRepository = equipmentRepository;
        this.baseRepository = baseRepository;
        this.userRepository = userRepository;
        this.assetStockRepository = assetStockRepository;
    }

    /**
     * Saves an Assignment record to the database after validating the input and stock.
     * Automatically assigns the currently authenticated user as assignedBy.
     * Validates that sufficient stock exists for the equipment at the base without
     * reducing the stock (personnel allocation, not consumption).
     *
     * Validation rules:
     *   - equipment must be provided
     *   - base must be provided
     *   - personnelName must be provided and not blank
     *   - quantity must be greater than 0
     *   - assignmentDate must be provided
     *   - asset stock must exist at the base
     *   - asset stock must have quantity >= assignment quantity
     *
     * @param assignment the Assignment object to save
     * @return the saved Assignment (now includes a generated id)
     * @throws IllegalArgumentException if any validation rule fails
     * @throws IllegalStateException if no authenticated user is found or insufficient stock
     */
    @Transactional
    public Assignment saveAssignment(Assignment assignment) {
        if (assignment.getEquipment() == null) {
            throw new IllegalArgumentException("Equipment is required.");
        }
        if (assignment.getBase() == null) {
            throw new IllegalArgumentException("Base is required.");
        }
        if (assignment.getPersonnelName() == null || assignment.getPersonnelName().trim().isEmpty()) {
            throw new IllegalArgumentException("Personnel name is required.");
        }
        if (assignment.getQuantity() == null || assignment.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (assignment.getAssignmentDate() == null) {
            throw new IllegalArgumentException("Assignment date is required.");
        }

        // Fetch full Equipment entity if name is missing
        if (assignment.getEquipment().getId() != null) {
            Equipment eq = equipmentRepository.findById(assignment.getEquipment().getId())
                    .orElse(assignment.getEquipment());
            assignment.setEquipment(eq);
        }

        // Fetch full Base entity if name is missing
        if (assignment.getBase().getId() != null) {
            Base b = baseRepository.findById(assignment.getBase().getId())
                    .orElse(assignment.getBase());
            assignment.setBase(b);
        }

        // Automatically assign authenticated user from SecurityContext
        User authenticatedUser = getAuthenticatedUser();
        assignment.setAssignedBy(authenticatedUser);

        // Enforce base-scope validation for BASE_COMMANDER
        if (authenticatedUser.getRole() == Role.BASE_COMMANDER || SecurityUtils.isBaseCommander()) {
            Base assignedBase = authenticatedUser.getAssignedBase();
            if (assignedBase == null || assignedBase.getId() == null
                    || !assignedBase.getId().equals(assignment.getBase().getId())) {
                throw new AccessDeniedException(
                        "Access denied: Base Commander is only authorized to assign equipment at their assigned base."
                );
            }
        }

        // Find AssetStock using equipmentId and baseId
        Long equipmentId = assignment.getEquipment().getId();
        Long baseId = assignment.getBase().getId();

        Optional<AssetStock> stockOpt = Optional.empty();
        if (equipmentId != null && baseId != null) {
            stockOpt = assetStockRepository.findByEquipment_IdAndBase_Id(equipmentId, baseId);
            if (stockOpt.isEmpty()) {
                stockOpt = assetStockRepository.findByEquipmentAndBase(assignment.getEquipment(), assignment.getBase());
            }
        } else {
            stockOpt = assetStockRepository.findByEquipmentAndBase(assignment.getEquipment(), assignment.getBase());
        }

        String equipmentName = (assignment.getEquipment().getName() != null && !assignment.getEquipment().getName().trim().isEmpty())
                ? assignment.getEquipment().getName()
                : "Equipment";
        String baseName = (assignment.getBase().getName() != null && !assignment.getBase().getName().trim().isEmpty())
                ? assignment.getBase().getName()
                : "Base";

        // Reject if no AssetStock exists
        if (stockOpt.isEmpty()) {
            throw new IllegalStateException("Insufficient stock: No " + equipmentName + " available at " + baseName + ".");
        }

        AssetStock stock = stockOpt.get();
        // Reject if stock quantity is less than requested assignment quantity
        if (stock.getQuantity() < assignment.getQuantity()) {
            throw new IllegalStateException("Insufficient stock: Only " + stock.getQuantity() + " " + equipmentName + " available at " + baseName + ".");
        }

        // Do NOT decrease AssetStock when an assignment is created (personnel allocation, not consumption).
        return assignmentRepository.save(assignment);
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
     * Retrieves all Assignment records from the database.
     *
     * @return a list of all Assignment objects
     */
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }
}
