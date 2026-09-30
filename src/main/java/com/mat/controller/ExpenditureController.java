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
 * REST API for expenditure tracking.
 */
@RestController
@RequestMapping("/api/expenditures")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    public ExpenditureController(ExpenditureService expenditureService) {
        this.expenditureService = expenditureService;
    }

    @PostMapping
    public ResponseEntity<Expenditure> createExpenditure(@RequestBody Expenditure expenditure) {
        Expenditure savedExpenditure = expenditureService.saveExpenditure(expenditure);
        return ResponseEntity.ok(savedExpenditure);
    }

    @GetMapping
    public ResponseEntity<List<Expenditure>> getAllExpenditures() {
        List<Expenditure> expenditures = expenditureService.getAllExpenditures();
        return ResponseEntity.ok(expenditures);
    }
}
