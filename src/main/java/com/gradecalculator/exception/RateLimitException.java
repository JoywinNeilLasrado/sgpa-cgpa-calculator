package com.gradecalculator.exception;

/**
 * Exception thrown when a client exceeds rate limits (e.g., login brute-force attempts).
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
