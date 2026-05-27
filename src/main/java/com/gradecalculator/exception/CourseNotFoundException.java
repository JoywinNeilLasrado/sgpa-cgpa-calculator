package com.gradecalculator.exception;

/**
 * Exception thrown when a requested course is not found.
 */
public class CourseNotFoundException extends BaseException {
    
    public CourseNotFoundException(Long id) {
        super("COURSE_NOT_FOUND", "Course not found with ID: " + id);
    }
    
    public CourseNotFoundException(String courseCode) {
        super("COURSE_NOT_FOUND", "Course not found with code: " + courseCode);
    }
    
    public CourseNotFoundException(String message, Throwable cause) {
        super("COURSE_NOT_FOUND", message, cause);
    }
}
