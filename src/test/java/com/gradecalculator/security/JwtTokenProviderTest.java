package com.gradecalculator.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String testSecret = "myextremelylongtestsecretkeythatismorethanthirtytwobyteslongforhs256!";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(testSecret, testExpiration);
    }

    @Test
    void throwsExceptionWhenSecretIsNullOrEmpty() {
        assertThatThrownBy(() -> new JwtTokenProvider(null, testExpiration))
                .isInstanceOf(IllegalStateException.class);
        
        assertThatThrownBy(() -> new JwtTokenProvider("   ", testExpiration))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generatesAndValidatesTokenSuccessfully() {
        String token = tokenProvider.generateToken(123L, "alice", "STUDENT");

        assertThat(token).isNotEmpty();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(123L);
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("alice");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo("STUDENT");
    }

    @Test
    void returnsFalseForInvalidToken() {
        String invalidToken = "completely.invalid.token";
        assertThat(tokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    void returnsFalseForTamperedToken() {
        String token = tokenProvider.generateToken(123L, "alice", "STUDENT");
        // Tamper with the token string
        String tamperedToken = token + "tampered";
        assertThat(tokenProvider.validateToken(tamperedToken)).isFalse();
    }
}
