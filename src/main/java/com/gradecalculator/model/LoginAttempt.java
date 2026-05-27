package com.gradecalculator.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for tracking persistent rate limiter attempts.
 */
@Entity
@Table(name = "login_attempts", indexes = {
    @Index(name = "idx_login_attempt_key", columnList = "attempt_key", unique = true)
})
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_key", nullable = false, unique = true)
    @NonNull
    private String attemptKey;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "last_attempt_time", nullable = false)
    private long lastAttemptTime;

    // Custom constructors
    public LoginAttempt(String attemptKey, int attempts, long lastAttemptTime) {
        this.attemptKey = attemptKey;
        this.attempts = attempts;
        this.lastAttemptTime = lastAttemptTime;
    }

    // Business logic
    public void incrementAttempts() {
        this.attempts++;
        this.lastAttemptTime = System.currentTimeMillis();
    }

    public void reset() {
        this.attempts = 0;
    }
}
