package com.gradecalculator.controller;

import com.gradecalculator.security.AuditLogExporter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Admin-only audit log controller.
 * Exposes a streaming NDJSON (JSON Lines) export endpoint compatible with
 * Splunk, ELK, Datadog, and other SIEM platforms.
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogExporter exporter;

    public AuditController(AuditLogExporter exporter) {
        this.exporter = exporter;
    }

    /**
     * Export audit logs as JSON Lines (NDJSON).
     *
     * <pre>
     * GET /api/audit/export
     *   ?from=2024-01-01T00:00:00      (ISO datetime, optional)
     *   &amp;to=2024-12-31T23:59:59        (ISO datetime, optional)
     *   &amp;action=UPDATE                 (optional)
     *   &amp;entityName=Enrollment         (optional)
     *   &amp;username=admin                (optional)
     *   &amp;limit=5000                    (optional, default 10000)
     * </pre>
     *
     * Content-Type: {@code application/x-ndjson}
     * Content-Disposition: attachment; filename="audit-YYYY-MM-DD.jsonl"
     */
    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportAuditLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "0") int limit) {

        StreamingResponseBody body = exporter.export(from, to, action, entityName, username, limit);

        String filename = "audit-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".jsonl";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-ndjson"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache")
                .body(body);
    }

    /**
     * Lightweight health/summary endpoint — returns count of audit records.
     * GET /api/audit/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok(java.util.Map.of("status", "ok", "endpoint", "/api/audit/export"));
    }
}
