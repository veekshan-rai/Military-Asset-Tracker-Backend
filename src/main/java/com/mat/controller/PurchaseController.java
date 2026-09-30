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
 * REST API for purchase procurement operations.
 */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<Purchase> createPurchase(@RequestBody Purchase purchase) {
        Purchase savedPurchase = purchaseService.savePurchase(purchase);
        return ResponseEntity.ok(savedPurchase);
    }

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
