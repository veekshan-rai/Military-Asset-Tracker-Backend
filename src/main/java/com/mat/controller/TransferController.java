package com.mat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mat.entity.Transfer;
import com.mat.service.TransferService;

/**
 * TransferController
 *
 * Handles HTTP requests related to Transfer operations (moving equipment between bases).
 *
 * Endpoints:
 *   POST /api/transfers   → Create a new transfer record
 *   GET  /api/transfers   → Retrieve all transfer history
 */
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    /**
     * Constructor injection of TransferService.
     */
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * POST /api/transfers
     *
     * Creates a new transfer record.
     * Validation (e.g. fromBase != toBase, quantity > 0) is performed by TransferService.
     *
     * @param transfer the Transfer object from the request body
     * @return the saved Transfer object
     */
    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@RequestBody Transfer transfer) {
        Transfer savedTransfer = transferService.saveTransfer(transfer);
        return ResponseEntity.ok(savedTransfer);
    }

    /**
     * GET /api/transfers
     *
     * Retrieves all transfer records from the database.
     *
     * @return a list of all Transfer objects
     */
    @GetMapping
    public ResponseEntity<List<Transfer>> getAllTransfers() {
        List<Transfer> transfers = transferService.getAllTransfers();
        return ResponseEntity.ok(transfers);
    }
}
