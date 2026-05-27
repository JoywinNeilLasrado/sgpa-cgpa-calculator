package com.gradecalculator.model;

import jakarta.persistence.*;

/**
 * Entity for tracking persistent rate limiter attempts.
 */
@Entity
@Table(name = "login_attempts", indexes = {
    @Index(name = "idx_login_attempt_key", columnList = "attempt_key", unique = true)
})
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_key", nullable = false, unique = true)
    private String attemptKey;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_attempt_time", nullable = false)
    private long lastAttemptTime;

    public LoginAttempt() {}

    public LoginAttempt(String attemptKey, int attempts, long lastAttemptTime) {
        this.attemptKey = attemptKey;
        this.attempts = attempts;
        this.lastAttemptTime = lastAttemptTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttemptKey() {
        return attemptKey;
    }

    public void setAttemptKey(String attemptKey) {
        this.attemptKey = attemptKey;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public long getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(long lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
    }
}
