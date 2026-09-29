package com.mat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.Equipment;
import com.mat.service.EquipmentService;

/**
 * EquipmentController
 *
 * Handles HTTP requests for managing equipment catalog entries.
 *
 * Endpoints:
 *   POST /api/equipment   → Create a new equipment type
 *   GET  /api/equipment   → Retrieve all equipment types
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    /**
     * POST /api/equipment
     *
     * Creates a new Equipment record.
     *
     * Example request body:
     * {
     *   "name": "AK-47",
     *   "equipmentType": "Weapon",
     *   "unit": "pieces"
     * }
     *
     * @param equipment the Equipment data from the request body
     * @return the saved Equipment object (with generated id)
     */
    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        Equipment savedEquipment = equipmentService.saveEquipment(equipment);
        return ResponseEntity.ok(savedEquipment);
    }

    /**
     * GET /api/equipment
     *
     * Retrieves all equipment records from the database.
     *
     * @return a list of all Equipment objects
     */
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        return ResponseEntity.ok(equipmentList);
    }
}
