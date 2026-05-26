package com.gradecalculator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when input validation fails.
 * Inherits from IllegalArgumentException to maintain full backward compatibility with tests.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends IllegalArgumentException {
    public ValidationException(String message) {
        super(message);
    }
}
