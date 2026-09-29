package com.mat.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.Purchase;
import com.mat.service.PurchaseService;

/**
 * PurchaseController
 *
 * Handles HTTP requests related to Purchase operations.
 * This controller is the entry point for the frontend (or any HTTP client)
 * to create and retrieve purchase records.
 *
 * Endpoints:
 *   POST /api/purchases                          → Create a new purchase record
 *   GET  /api/purchases                          → Retrieve all purchases
 *   GET  /api/purchases?equipmentType=Weapon      → Filter by equipment type
 *   GET  /api/purchases?date=2026-09-28           → Filter by date
 *
 * The controller delegates all business logic to PurchaseService.
 * It does NOT contain any business rules, database calls, or validation logic.
 */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Constructor injection.
     * Spring automatically provides the PurchaseService instance.
     */
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * POST /api/purchases
     *
     * Creates a new purchase record.
     * The request body must contain: equipment, base, quantity, purchaseDate.
     * Validation is handled by PurchaseService.
     *
     * @param purchase the Purchase data from the request body
     * @return the saved Purchase object (with generated id)
     */
    @PostMapping
    public ResponseEntity<Purchase> createPurchase(@RequestBody Purchase purchase) {
        Purchase savedPurchase = purchaseService.savePurchase(purchase);
        return ResponseEntity.ok(savedPurchase);
    }

    /**
     * GET /api/purchases
     *
     * Retrieves purchase records. Supports optional filtering:
     *   - equipmentType: filter by equipment type (e.g., "Weapon", "Vehicle")
     *   - date: filter by specific date (e.g., "2026-09-28")
     *
     * If no filters are provided, returns all purchases.
     *
     * @param equipmentType optional filter by equipment type
     * @param date          optional filter by date (yyyy-MM-dd format)
     * @return a list of Purchase objects
     */
    @GetMapping
    public ResponseEntity<List<Purchase>> getPurchases(
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) LocalDate date) {

        List<Purchase> purchases;

        if (equipmentType != null) {
            purchases = purchaseService.getPurchasesByEquipmentType(equipmentType);
        } else if (date != null) {
            purchases = purchaseService.getPurchasesByDate(date);
        } else {
            purchases = purchaseService.getAllPurchases();
        }

        return ResponseEntity.ok(purchases);
    }
}
