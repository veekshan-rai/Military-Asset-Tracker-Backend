package com.mat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.Expenditure;
import com.mat.service.ExpenditureService;

/**
 * ExpenditureController
 *
 * Handles HTTP requests related to Expenditure operations (equipment consumption/loss).
 *
 * Endpoints:
 *   POST /api/expenditures   → Create a new expenditure record
 *   GET  /api/expenditures   → Retrieve all expenditure history
 */
@RestController
@RequestMapping("/api/expenditures")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    /**
     * Constructor injection of ExpenditureService.
     */
    public ExpenditureController(ExpenditureService expenditureService) {
        this.expenditureService = expenditureService;
    }

    /**
     * POST /api/expenditures
     *
     * Creates a new expenditure record.
     * Validation (e.g. reason required, quantity > 0) is performed by ExpenditureService.
     *
     * @param expenditure the Expenditure object from the request body
     * @return the saved Expenditure object
     */
    @PostMapping
    public ResponseEntity<Expenditure> createExpenditure(@RequestBody Expenditure expenditure) {
        Expenditure savedExpenditure = expenditureService.saveExpenditure(expenditure);
        return ResponseEntity.ok(savedExpenditure);
    }

    /**
     * GET /api/expenditures
     *
     * Retrieves all expenditure records from the database.
     *
     * @return a list of all Expenditure objects
     */
    @GetMapping
    public ResponseEntity<List<Expenditure>> getAllExpenditures() {
        List<Expenditure> expenditures = expenditureService.getAllExpenditures();
        return ResponseEntity.ok(expenditures);
    }
}
