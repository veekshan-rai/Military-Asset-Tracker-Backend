package com.mat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.Assignment;
import com.mat.service.AssignmentService;

/**
 * AssignmentController
 *
 * Handles HTTP requests related to Assignment operations (assigning equipment to personnel).
 *
 * Endpoints:
 *   POST /api/assignments   → Create a new assignment record
 *   GET  /api/assignments   → Retrieve all assignment history
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Constructor injection of AssignmentService.
     */
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * POST /api/assignments
     *
     * Creates a new assignment record.
     * Validation (e.g. personnelName required, quantity > 0) is performed by AssignmentService.
     *
     * @param assignment the Assignment object from the request body
     * @return the saved Assignment object
     */
    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        Assignment savedAssignment = assignmentService.saveAssignment(assignment);
        return ResponseEntity.ok(savedAssignment);
    }

    /**
     * GET /api/assignments
     *
     * Retrieves all assignment records from the database.
     *
     * @return a list of all Assignment objects
     */
    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        List<Assignment> assignments = assignmentService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }
}
