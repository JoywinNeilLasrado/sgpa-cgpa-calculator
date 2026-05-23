package com.gradecalculator.security;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe in-memory login rate limiter to protect against brute-force attacks.
 */
@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(15); // Block for 15 minutes

    private final ConcurrentHashMap<String, AttemptTracker> trackers = new ConcurrentHashMap<>();

    /**
     * Check if the specified client IP is currently blocked.
     */
    public boolean isBlocked(String ip) {
        if (ip == null) {
            return false;
        }
        AttemptTracker tracker = trackers.get(ip);
        if (tracker == null) {
            return false;
        }
        if (tracker.isBlocked(BLOCK_DURATION_MS)) {
            return true;
        }
        // If blocking period has expired, automatically clean up tracker
        if (System.currentTimeMillis() - tracker.lastAttemptTime > BLOCK_DURATION_MS) {
            trackers.remove(ip);
            return false;
        }
        return false;
    }

    /**
     * Record a failed login attempt for the client IP.
     */
    public void loginFailed(String ip) {
        if (ip == null) {
            return;
        }
        trackers.compute(ip, (key, value) -> {
            long now = System.currentTimeMillis();
            if (value == null) {
                return new AttemptTracker(1, now);
            }
            if (now - value.lastAttemptTime > BLOCK_DURATION_MS) {
                return new AttemptTracker(1, now); // Reset if block duration has elapsed
            }
            return new AttemptTracker(value.attempts + 1, now);
        });
    }

    /**
     * Clear failed attempts upon successful login.
     */
    public void loginSucceeded(String ip) {
        if (ip != null) {
            trackers.remove(ip);
        }
    }

    /**
     * Tracker object to store attempt count and last action timestamp.
     */
    private static class AttemptTracker {
        final int attempts;
        final long lastAttemptTime;

        AttemptTracker(int attempts, long lastAttemptTime) {
            this.attempts = attempts;
            this.lastAttemptTime = lastAttemptTime;
        }

        boolean isBlocked(long blockDurationMs) {
            return attempts >= MAX_ATTEMPTS && (System.currentTimeMillis() - lastAttemptTime < blockDurationMs);
        }
    }
}
