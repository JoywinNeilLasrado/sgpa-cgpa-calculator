package com.gradecalculator.controller;

import com.gradecalculator.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller exposing system-to-system integration tools protected by the X-API-KEY header check.
 */
@RestController
@RequestMapping("/api/internal")
public class InternalServiceController {

    private final AuditLogRepository auditLogRepository;

    public InternalServiceController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Get System Health Status - GET /api/internal/system-status
     */
    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("serviceName", "sgpa-cgpa-calculator-internal");
        status.put("activeDatabase", "H2 (Development)");
        status.put("totalSecurityAuditTrailRecords", auditLogRepository.count());
        status.put("jvmFreeMemoryBytes", Runtime.getRuntime().freeMemory());
        status.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }

    /**
     * Trigger Administrative Mock Backup - POST /api/internal/trigger-backup
     */
    @PostMapping("/trigger-backup")
    public ResponseEntity<Map<String, Object>> triggerBackup() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("message", "Simulated system database backup triggered successfully!");
        result.put("recordsProcessed", auditLogRepository.count());
        result.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(result);
    }
}
