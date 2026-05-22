package com.gradecalculator.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for creating a new enrollment.
 */
public class EnrollmentRequest {

    @NotNull(message = "Student ID cannot be null")
    private Long studentId;

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;

    @Pattern(regexp = "^(O|A\\+|A|B\\+|B|C|P|F|)$", message = "Grade must be one of: O, A+, A, B+, B, C, P, F, or empty")
    private String grade; // Letter grade as string (O, A+, A, B+, B, C, P, F)

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}