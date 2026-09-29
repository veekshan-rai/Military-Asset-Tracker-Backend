package com.mat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.Base;
import com.mat.service.BaseService;

/**
 * BaseController
 *
 * Handles HTTP requests for managing military bases.
 *
 * Endpoints:
 *   POST /api/bases   → Create a new base
 *   GET  /api/bases   → Retrieve all bases
 */
@RestController
@RequestMapping("/api/bases")
public class BaseController {

    private final BaseService baseService;

    public BaseController(BaseService baseService) {
        this.baseService = baseService;
    }

    /**
     * POST /api/bases
     *
     * Creates a new Base record.
     *
     * Example request body:
     * {
     *   "name": "Base Alpha",
     *   "location": "Northern Region"
     * }
     *
     * @param base the Base data from the request body
     * @return the saved Base object (with generated id)
     */
    @PostMapping
    public ResponseEntity<Base> createBase(@RequestBody Base base) {
        Base savedBase = baseService.saveBase(base);
        return ResponseEntity.ok(savedBase);
    }

    /**
     * GET /api/bases
     *
     * Retrieves all base records from the database.
     *
     * @return a list of all Base objects
     */
    @GetMapping
    public ResponseEntity<List<Base>> getAllBases() {
        List<Base> bases = baseService.getAllBases();
        return ResponseEntity.ok(bases);
    }
}
