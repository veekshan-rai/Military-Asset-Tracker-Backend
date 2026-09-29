package com.mat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mat.entity.Assignment;
import com.mat.repository.AssignmentRepository;

/**
 * AssignmentService
 *
 * Contains the business logic for managing Assignment records.
 * Acts as the middle layer between the Controller and the Repository.
 *
 * Provides:
 *   - Saving an assignment with validation
 *   - Retrieving all assignments
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Saves an Assignment record to the database after validating the input.
     *
     * Validation rules:
     *   - equipment must be provided
     *   - base must be provided
     *   - personnelName must be provided and not blank
     *   - quantity must be greater than 0
     *   - assignmentDate must be provided
     *
     * @param assignment the Assignment object to save
     * @return the saved Assignment (now includes a generated id)
     * @throws IllegalArgumentException if any validation rule fails
     */
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

        return assignmentRepository.save(assignment);
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
