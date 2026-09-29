package com.mat.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mat.dto.DashboardResponse;
import com.mat.entity.User;
import com.mat.repository.UserRepository;
import com.mat.security.SecurityUtils;
import com.mat.service.DashboardService;

/**
 * DashboardController
 *
 * Provides the dashboard API for viewing asset metrics.
 * Restricts BASE_COMMANDER users to their assigned base.
 *
 * Endpoints:
 *   GET /api/dashboard                                    → All metrics (system-wide)
 *   GET /api/dashboard?baseId=1                           → Metrics for a specific base
 *   GET /api/dashboard?date=2026-09-28                    → Metrics for a specific date
 *   GET /api/dashboard?equipmentType=Weapon               → Metrics for a specific equipment type
 *   GET /api/dashboard?baseId=1&date=2026-09-28           → Combined filters
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/dashboard
     *
     * Returns dashboard metrics with optional filters.
     * Enforces base restriction for BASE_COMMANDER.
     *
     * @param baseId        optional base ID filter
     * @param equipmentType optional equipment type filter
     * @param date          optional date filter (yyyy-MM-dd)
     * @return DashboardResponse with all calculated metrics
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) LocalDate date) {

        // RBAC restriction: BASE_COMMANDER is restricted to their assigned base
        if (SecurityUtils.isBaseCommander()) {
            String username = SecurityUtils.getCurrentUsername();
            if (username != null) {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && user.getAssignedBase() != null) {
                    baseId = user.getAssignedBase().getId();
                }
            }
        }

        DashboardResponse response = dashboardService.getDashboard(baseId, equipmentType, date);
        return ResponseEntity.ok(response);
    }
}
