package com.gradecalculator.exception;

/**
 * Exception thrown when a requested user is not found.
 */
public class UserNotFoundException extends BaseException {
    
    public UserNotFoundException(Long id) {
        super("USER_NOT_FOUND", "User not found with ID: " + id);
    }
    
    public UserNotFoundException(String username) {
        super("USER_NOT_FOUND", "User not found with username: " + username);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super("USER_NOT_FOUND", message, cause);
    }
}
