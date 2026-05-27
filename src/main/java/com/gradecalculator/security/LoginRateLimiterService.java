package com.gradecalculator.security;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe hybrid rate limiter to protect against brute-force attacks.
 * Uses a Redis backing store if available to persist attempts across restarts,
 * and falls back to an in-memory ConcurrentHashMap for local testing and standalone runs.
 */
@Service
public class LoginRateLimiterService {

    public static final int MAX_ATTEMPTS = 5;
    public static final long BLOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(1); // Block for 1 minute

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private final ConcurrentHashMap<String, AttemptTracker> trackers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptTracker> accountTrackers = new ConcurrentHashMap<>();

    /**
     * Check if the specified client IP is currently blocked.
     */
    public boolean isBlocked(String ip) {
        if (ip == null) {
            return false;
        }
        if (redisTemplate != null) {
            try {
                String val = redisTemplate.opsForValue().get("rate:ip:" + ip);
                if (val != null) {
                    return Integer.parseInt(val) >= MAX_ATTEMPTS;
                }
                return false;
            } catch (Exception e) {
                // Connection failures fall through to in-memory map
            }
        }
        AttemptTracker tracker = trackers.get(ip);
        if (tracker == null) {
            return false;
        }
        if (tracker.isBlocked(BLOCK_DURATION_MS)) {
            return true;
        }
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
        if (redisTemplate != null) {
            try {
                String val = redisTemplate.opsForValue().get("rate:user:" + username);
                if (val != null) {
                    return Integer.parseInt(val) >= MAX_ATTEMPTS;
                }
                return false;
            } catch (Exception e) {
                // Connection failures fall through to in-memory map
            }
        }
        AttemptTracker tracker = accountTrackers.get(username);
        if (tracker == null) {
            return false;
        }
        if (tracker.isBlocked(BLOCK_DURATION_MS)) {
            return true;
        }
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
        if (redisTemplate != null) {
            try {
                String key = "rate:ip:" + ip;
                Long attempts = redisTemplate.opsForValue().increment(key);
                if (attempts != null && attempts == 1) {
                    redisTemplate.expire(key, BLOCK_DURATION_MS, TimeUnit.MILLISECONDS);
                }
                return;
            } catch (Exception e) {
                // Connection failures fall through to in-memory map
            }
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
        if (redisTemplate != null) {
            try {
                String key = "rate:user:" + username;
                Long attempts = redisTemplate.opsForValue().increment(key);
                if (attempts != null && attempts == 1) {
                    redisTemplate.expire(key, BLOCK_DURATION_MS, TimeUnit.MILLISECONDS);
                }
                return;
            } catch (Exception e) {
                // Connection failures fall through to in-memory map
            }
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
            if (redisTemplate != null) {
                try {
                    redisTemplate.delete("rate:ip:" + ip);
                    return;
                } catch (Exception e) {
                    // fall through
                }
            }
            trackers.remove(ip);
        }
    }

    /**
     * Clear failed attempts upon successful login for the username.
     */
    public void accountLoginSucceeded(String username) {
        if (username != null) {
            if (redisTemplate != null) {
                try {
                    redisTemplate.delete("rate:user:" + username);
                    return;
                } catch (Exception e) {
                    // fall through
                }
            }
            accountTrackers.remove(username);
        }
    }

    /**
     * Returns the current failed attempt count for a username.
     */
    public int getAccountAttempts(String username) {
        if (username == null) return 0;
        if (redisTemplate != null) {
            try {
                String val = redisTemplate.opsForValue().get("rate:user:" + username);
                return val == null ? 0 : Integer.parseInt(val);
            } catch (Exception e) {
                // fall through
            }
        }
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
        if (redisTemplate != null) {
            try {
                String val = redisTemplate.opsForValue().get("rate:user:" + username);
                if (val != null && Integer.parseInt(val) >= MAX_ATTEMPTS) {
                    Long ttl = redisTemplate.getExpire("rate:user:" + username, TimeUnit.MILLISECONDS);
                    return ttl != null ? Math.max(0, ttl) : 0;
                }
                return 0;
            } catch (Exception e) {
                // fall through
            }
        }
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
