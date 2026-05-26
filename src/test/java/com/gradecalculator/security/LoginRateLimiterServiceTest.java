package com.gradecalculator.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterServiceTest {

    private LoginRateLimiterService limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginRateLimiterService();
    }

    @Test
    void newIpAddressIsNotBlocked() {
        assertThat(limiter.isBlocked("192.168.1.1")).isFalse();
    }

    @Test
    void isBlockedAfterMaxAttempts() {
        String ip = "10.0.0.1";
        
        // 4 failed attempts should not block
        for (int i = 0; i < 4; i++) {
            limiter.loginFailed(ip);
            assertThat(limiter.isBlocked(ip)).isFalse();
        }

        // 5th failed attempt should trigger block
        limiter.loginFailed(ip);
        assertThat(limiter.isBlocked(ip)).isTrue();
    }

    @Test
    void successClearsAttempts() {
        String ip = "172.16.0.1";
        
        for (int i = 0; i < 4; i++) {
            limiter.loginFailed(ip);
        }
        
        // Successful login should clear counter
        limiter.loginSucceeded(ip);
        assertThat(limiter.isBlocked(ip)).isFalse();
        
        // Failure counter resets to 0, so another failure doesn't block
        limiter.loginFailed(ip);
        assertThat(limiter.isBlocked(ip)).isFalse();
    }

    @Test
    void nullIpSafelyIgnored() {
        assertThat(limiter.isBlocked(null)).isFalse();
        limiter.loginFailed(null);
        limiter.loginSucceeded(null);
    }

    @Test
    void newAccountIsNotBlocked() {
        assertThat(limiter.isAccountLocked("user1")).isFalse();
    }

    @Test
    void isAccountBlockedAfterMaxAttempts() {
        String username = "attacker";
        
        // 4 failed attempts should not lock account
        for (int i = 0; i < 4; i++) {
            limiter.accountLoginFailed(username);
            assertThat(limiter.isAccountLocked(username)).isFalse();
        }

        // 5th failed attempt should lock account
        limiter.accountLoginFailed(username);
        assertThat(limiter.isAccountLocked(username)).isTrue();
    }

    @Test
    void accountSuccessClearsAttempts() {
        String username = "student1";
        
        for (int i = 0; i < 4; i++) {
            limiter.accountLoginFailed(username);
        }
        
        // Successful login should clear counter
        limiter.accountLoginSucceeded(username);
        assertThat(limiter.isAccountLocked(username)).isFalse();
        
        // Failure counter resets to 0, so another failure doesn't lock
        limiter.accountLoginFailed(username);
        assertThat(limiter.isAccountLocked(username)).isFalse();
    }

    @Test
    void nullAccountSafelyIgnored() {
        assertThat(limiter.isAccountLocked(null)).isFalse();
        limiter.accountLoginFailed(null);
        limiter.accountLoginSucceeded(null);
    }
}
