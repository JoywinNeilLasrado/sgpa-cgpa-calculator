package com.gradecalculator.exception;

/**
 * Exception thrown when an enrollment is not found or invalid.
 */
public class EnrollmentNotFoundException extends BaseException {
    
    public EnrollmentNotFoundException(Long id) {
        super("ENROLLMENT_NOT_FOUND", "Enrollment not found with ID: " + id);
    }
    
    public EnrollmentNotFoundException(String message) {
        super("ENROLLMENT_NOT_FOUND", message);
    }
    
    public EnrollmentNotFoundException(String message, Throwable cause) {
        super("ENROLLMENT_NOT_FOUND", message, cause);
    }
}
