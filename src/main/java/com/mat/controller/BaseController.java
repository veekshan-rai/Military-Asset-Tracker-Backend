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
 * REST API for military base management.
 */
@RestController
@RequestMapping("/api/bases")
public class BaseController {

    private final BaseService baseService;

    public BaseController(BaseService baseService) {
        this.baseService = baseService;
    }

    @PostMapping
    public ResponseEntity<Base> createBase(@RequestBody Base base) {
        Base savedBase = baseService.saveBase(base);
        return ResponseEntity.ok(savedBase);
    }

    @GetMapping
    public ResponseEntity<List<Base>> getAllBases() {
        List<Base> bases = baseService.getAllBases();
        return ResponseEntity.ok(bases);
    }
}
