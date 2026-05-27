package com.gradecalculator.model;

import jakarta.persistence.*;
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

    public AuditLog() {}

    public AuditLog(LocalDateTime timestamp, String username, String ip, String action, String entityName, String entityId, String previousValue, String newValue) {
        this.timestamp = timestamp;
        this.username = username;
        this.ip = ip;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.previousValue = previousValue != null && previousValue.length() > 2048 ? previousValue.substring(0, 2048) : previousValue;
        this.newValue = newValue != null && newValue.length() > 2048 ? newValue.substring(0, 2048) : newValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue != null && previousValue.length() > 2048 ? previousValue.substring(0, 2048) : previousValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue != null && newValue.length() > 2048 ? newValue.substring(0, 2048) : newValue;
    }
}
