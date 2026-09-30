package com.mat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.AssetStock;
import com.mat.service.AssetStockService;

/**
 * REST API for querying current stock levels across bases with role-based access control.
 */
@RestController
@RequestMapping("/api/stock")
public class AssetStockController {

    private final AssetStockService assetStockService;

    public AssetStockController(AssetStockService assetStockService) {
        this.assetStockService = assetStockService;
    }

    /**
     * GET /api/stock
     *
     * Retrieves stock records, optionally filtered by baseId.
     * Enforces role-based filtering:
     *   - ADMIN: can see stock for all bases (or filtered by baseId if provided)
     *   - BASE_COMMANDER: can see ONLY stock for their assignedBase
     *   - LOGISTICS_OFFICER: existing behavior (all bases or filtered by baseId)
     */
    @GetMapping
    public ResponseEntity<List<AssetStock>> getStock(@RequestParam(required = false) Long baseId) {
        return ResponseEntity.ok(assetStockService.getStock(baseId));
    }

    /**
     * GET /api/stock/base/{baseId}
     *
     * Retrieves stock records for a specific base.
     * Enforces BASE_COMMANDER restriction to their assigned base.
     */
    @GetMapping("/base/{baseId}")
    public ResponseEntity<List<AssetStock>> getStockByBase(@PathVariable Long baseId) {
        return ResponseEntity.ok(assetStockService.getStockByBase(baseId));
    }
}
