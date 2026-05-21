package com.gradecalculator.dto;

/**
 * DTO for creating a new enrollment.
 */
public class EnrollmentRequest {

    private Long studentId;
    private Long courseId;
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