package com.gradecalculator.security;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe in-memory login rate limiter to protect against brute-force attacks.
 */
@Service
public class LoginRateLimiterService {

    public static final int MAX_ATTEMPTS = 5;
    public static final long BLOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(1); // Block for 1 minute

    private final ConcurrentHashMap<String, AttemptTracker> trackers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptTracker> accountTrackers = new ConcurrentHashMap<>();

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
     * Check if the specified username is currently locked.
     */
    public boolean isAccountLocked(String username) {
        if (username == null) {
            return false;
        }
        AttemptTracker tracker = accountTrackers.get(username);
        if (tracker == null) {
            return false;
        }
        if (tracker.isBlocked(BLOCK_DURATION_MS)) {
            return true;
        }
        // If blocking period has expired, automatically clean up tracker
        if (System.currentTimeMillis() - tracker.lastAttemptTime > BLOCK_DURATION_MS) {
            accountTrackers.remove(username);
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
     * Record a failed login attempt for the username.
     */
    public void accountLoginFailed(String username) {
        if (username == null) {
            return;
        }
        accountTrackers.compute(username, (key, value) -> {
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
     * Clear failed attempts upon successful login for the username.
     */
    public void accountLoginSucceeded(String username) {
        if (username != null) {
            accountTrackers.remove(username);
        }
    }

    /**
     * Returns the current failed attempt count for a username.
     */
    public int getAccountAttempts(String username) {
        if (username == null) return 0;
        AttemptTracker tracker = accountTrackers.get(username);
        if (tracker == null) return 0;
        // If the lockout window has expired, treat as 0
        if (System.currentTimeMillis() - tracker.lastAttemptTime > BLOCK_DURATION_MS) return 0;
        return tracker.attempts;
    }

    /**
     * Returns how many milliseconds remain in the lockout, or 0 if not locked.
     */
    public long getAccountLockoutRemainingMs(String username) {
        if (username == null) return 0;
        AttemptTracker tracker = accountTrackers.get(username);
        if (tracker == null || tracker.attempts < MAX_ATTEMPTS) return 0;
        long elapsed = System.currentTimeMillis() - tracker.lastAttemptTime;
        long remaining = BLOCK_DURATION_MS - elapsed;
        return Math.max(0, remaining);
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
