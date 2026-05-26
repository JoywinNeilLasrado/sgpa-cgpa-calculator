package com.gradecalculator.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * Global Security Audit Logger for tracking critical domain data changes.
 */
public class AuditLogger {
    private static final Logger logger = LoggerFactory.getLogger("AuditLogger");

    /**
     * Logs an audit record for an entity state modification.
     *
     * @param entityName    the name of the entity being changed (e.g. "Enrollment")
     * @param entityId      the identifier of the entity
     * @param action        the modification action ("CREATE", "UPDATE", "DELETE", etc.)
     * @param previousValue a text representation of the pre-change properties
     * @param newValue      a text representation of the post-change properties
     */
    public static void logChangeEvent(String entityName, String entityId, String action, String previousValue, String newValue) {
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        String ip = "unknown";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
                    ip = xForwardedFor.split(",")[0].trim();
                } else {
                    ip = request.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            // Keep safe fallback if accessed out of request-handling scope
        }

        logger.info("AUDIT TRAIL | Timestamp: '{}' | User: '{}' | IP: '{}' | Action: '{}' on {} [{}] | Previous: '{}' | New: '{}'",
                LocalDateTime.now(), username, ip, action, entityName, entityId, previousValue, newValue);
    }
}
