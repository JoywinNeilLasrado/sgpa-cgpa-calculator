package com.gradecalculator.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardised REST error response payload.
 * Provides unified, informative diagnostic details for all client errors and exceptions.
 */
public record ErrorResponse(
    String status,
    int code,
    String message,
    String path,
    LocalDateTime timestamp,
    List<String> details,
    boolean success
) {
    public ErrorResponse(String status, int code, String message, String path, LocalDateTime timestamp, List<String> details) {
        this(status, code, message, path, timestamp, details, false);
    }
}
