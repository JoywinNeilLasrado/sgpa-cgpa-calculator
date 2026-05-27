package com.gradecalculator.security;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.gradecalculator.model.LoginAttempt;
import com.gradecalculator.repository.LoginAttemptRepository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

/**
 * Thread-safe hybrid rate limiter to protect against brute-force attacks.
 * Uses a Redis backing store if available to persist attempts across restarts,
 * falls back to a relational JPA database table for persistent storage without Redis,
 * and uses an in-memory ConcurrentHashMap as a last-resort fallback.
 */
@Service
@Transactional
public class LoginRateLimiterService {

    public static final int MAX_ATTEMPTS = 5;
    public static final long BLOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(1); // Block for 1 minute

    @Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    private LoginAttemptRepository loginAttemptRepository;

    private final ConcurrentHashMap<String, AttemptTracker> trackers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptTracker> accountTrackers = new ConcurrentHashMap<>();

    private AttemptTracker getFallbackTracker(String key, boolean isAccount) {
        ConcurrentHashMap<String, AttemptTracker> map = isAccount ? accountTrackers : trackers;
        return map.get(key);
    }

    private void saveFallbackTracker(String key, int attempts, long time, boolean isAccount) {
        ConcurrentHashMap<String, AttemptTracker> map = isAccount ? accountTrackers : trackers;
        map.put(key, new AttemptTracker(attempts, time));
    }

    private void removeFallbackTracker(String key, boolean isAccount) {
        ConcurrentHashMap<String, AttemptTracker> map = isAccount ? accountTrackers : trackers;
        map.remove(key);
    }

    private AttemptTracker getTracker(String key, boolean isAccount) {
        if (loginAttemptRepository != null) {
            try {
                Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByAttemptKey(key);
                if (attemptOpt.isPresent()) {
                    LoginAttempt attempt = attemptOpt.get();
                    return new AttemptTracker(attempt.getAttempts(), attempt.getLastAttemptTime());
                }
            } catch (Exception e) {
                // fall through to in-memory
            }
        }
        return getFallbackTracker(key, isAccount);
    }

    private void saveTracker(String key, int attempts, long time, boolean isAccount) {
        if (loginAttemptRepository != null) {
            try {
                LoginAttempt attempt = loginAttemptRepository.findByAttemptKey(key)
                        .orElse(new LoginAttempt(key, 0, 0));
                attempt.setAttempts(attempts);
                attempt.setLastAttemptTime(time);
                loginAttemptRepository.save(attempt);
                return;
            } catch (Exception e) {
                // fall through to in-memory
            }
        }
        saveFallbackTracker(key, attempts, time, isAccount);
    }

    private void removeTracker(String key, boolean isAccount) {
        if (loginAttemptRepository != null) {
            try {
                loginAttemptRepository.deleteByAttemptKey(key);
                return;
            } catch (Exception e) {
                // fall through to in-memory
            }
        }
        removeFallbackTracker(key, isAccount);
    }

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
                // Connection failures fall through to JPA/in-memory
            }
        }
        AttemptTracker tracker = getTracker("rate:ip:" + ip, false);
        if (tracker == null) {
            return false;
        }
        if (tracker.isBlocked(BLOCK_DURATION_MS)) {
            return true;
        }
        if (System.currentTimeMillis() - tracker.lastAttemptTime > BLOCK_DURATION_MS) {
            removeTracker("rate:ip:" + ip, false);
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
                // Connection failures fall through to JPA/in-memory
            }
        }
        AttemptTracker tracker = getTracker("rate:user:" + username, true);
        if (tracker == null) {
            return false;
        }
        if (tracker.isBlocked(BLOCK_DURATION_MS)) {
            return true;
        }
        if (System.currentTimeMillis() - tracker.lastAttemptTime > BLOCK_DURATION_MS) {
            removeTracker("rate:user:" + username, true);
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
                // Connection failures fall through to JPA/in-memory
            }
        }
        String key = "rate:ip:" + ip;
        AttemptTracker tracker = getTracker(key, false);
        long now = System.currentTimeMillis();
        if (tracker == null || (now - tracker.lastAttemptTime > BLOCK_DURATION_MS)) {
            saveTracker(key, 1, now, false);
        } else {
            saveTracker(key, tracker.attempts + 1, now, false);
        }
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
                // Connection failures fall through to JPA/in-memory
            }
        }
        String key = "rate:user:" + username;
        AttemptTracker tracker = getTracker(key, true);
        long now = System.currentTimeMillis();
        if (tracker == null || (now - tracker.lastAttemptTime > BLOCK_DURATION_MS)) {
            saveTracker(key, 1, now, true);
        } else {
            saveTracker(key, tracker.attempts + 1, now, true);
        }
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
            removeTracker("rate:ip:" + ip, false);
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
            removeTracker("rate:user:" + username, true);
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
        AttemptTracker tracker = getTracker("rate:user:" + username, true);
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
        AttemptTracker tracker = getTracker("rate:user:" + username, true);
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
