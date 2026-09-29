package com.mat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HealthController
 *
 * Provides a simple health-check endpoint to verify the backend is running.
 * This is often the first endpoint tested when setting up or deploying the application.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     *
     * Returns a JSON response confirming the server is up and running.
     * No authentication required — used by monitoring tools or the React frontend
     * to verify connectivity before performing any operations.
     *
     * @return 200 OK with a simple status payload
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "Military Asset Tracker (MAT)");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Backend is running successfully.");
        return ResponseEntity.ok(response);
    }
}
