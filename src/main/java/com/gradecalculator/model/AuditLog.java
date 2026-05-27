package com.gradecalculator.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a persistent record of security audit log changes.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_logs_username", columnList = "username"),
    @Index(name = "idx_audit_logs_entity", columnList = "entity_name, entity_id")
})
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "previous_value", length = 2048)
    private String previousValue;

    @Column(name = "new_value", length = 2048)
    private String newValue;

    // Custom constructor with value truncation
    public AuditLog(LocalDateTime timestamp, String username, String ip, String action, 
                    String entityName, String entityId, String previousValue, String newValue) {
        this.timestamp = timestamp;
        this.username = username;
        this.ip = ip;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.previousValue = truncateIfNeeded(previousValue, 2048);
        this.newValue = truncateIfNeeded(newValue, 2048);
    }

    private String truncateIfNeeded(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
