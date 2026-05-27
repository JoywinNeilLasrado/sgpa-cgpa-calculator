package com.gradecalculator.exception;

/**
 * Exception thrown when a requested semester is not found.
 */
public class SemesterNotFoundException extends BaseException {
    
    public SemesterNotFoundException(Long id) {
        super("SEMESTER_NOT_FOUND", "Semester not found with ID: " + id);
    }
    
    public SemesterNotFoundException(int semesterNumber) {
        super("SEMESTER_NOT_FOUND", "Semester not found: Semester " + semesterNumber);
    }
    
    public SemesterNotFoundException(String message, Throwable cause) {
        super("SEMESTER_NOT_FOUND", message, cause);
    }
}
