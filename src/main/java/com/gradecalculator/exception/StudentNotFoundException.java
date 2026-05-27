package com.gradecalculator.exception;

/**
 * Exception thrown when a requested student is not found.
 */
public class StudentNotFoundException extends BaseException {
    
    public StudentNotFoundException(Long id) {
        super("STUDENT_NOT_FOUND", "Student not found with ID: " + id);
    }
    
    public StudentNotFoundException(String studentId) {
        super("STUDENT_NOT_FOUND", "Student not found with roll number: " + studentId);
    }
    
    public StudentNotFoundException(String message, Throwable cause) {
        super("STUDENT_NOT_FOUND", message, cause);
    }
}
