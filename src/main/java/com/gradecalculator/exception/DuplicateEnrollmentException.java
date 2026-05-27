package com.gradecalculator.exception;

/**
 * Exception thrown when a duplicate enrollment is attempted.
 */
public class DuplicateEnrollmentException extends BaseException {
    
    public DuplicateEnrollmentException(Long studentId, Long courseId) {
        super("DUPLICATE_ENROLLMENT", 
              "Student " + studentId + " is already enrolled in course " + courseId);
    }
    
    public DuplicateEnrollmentException(String message) {
        super("DUPLICATE_ENROLLMENT", message);
    }
}
