package com.gradecalculator.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new enrollment or updating marks.
 */
public class EnrollmentRequest {

    @NotNull(message = "Student ID cannot be null")
    private Long studentId;

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;

    @Min(value = 0, message = "CIE marks cannot be negative")
    @Max(value = 50, message = "CIE marks cannot exceed 50")
    private Integer cieMarks;

    @Min(value = 0, message = "CIE Theory marks cannot be negative")
    @Max(value = 30, message = "CIE Theory marks cannot exceed 30")
    private Integer cieTheoryMarks;

    @Min(value = 0, message = "CIE Lab marks cannot be negative")
    @Max(value = 20, message = "CIE Lab marks cannot exceed 20")
    private Integer cieLabMarks;

    @Min(value = 0, message = "SEE marks cannot be negative")
    @Max(value = 50, message = "SEE marks cannot exceed 50")
    private Integer seeMarks;

    @Min(value = 0, message = "Grace marks cannot be negative")
    @Max(value = 5, message = "Grace marks cannot exceed 5")
    private Integer graceMarks;

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

    public Integer getCieMarks() {
        return cieMarks;
    }

    public void setCieMarks(Integer cieMarks) {
        this.cieMarks = cieMarks;
    }

    public Integer getCieTheoryMarks() {
        return cieTheoryMarks;
    }

    public void setCieTheoryMarks(Integer cieTheoryMarks) {
        this.cieTheoryMarks = cieTheoryMarks;
    }

    public Integer getCieLabMarks() {
        return cieLabMarks;
    }

    public void setCieLabMarks(Integer cieLabMarks) {
        this.cieLabMarks = cieLabMarks;
    }

    public Integer getSeeMarks() {
        return seeMarks;
    }

    public void setSeeMarks(Integer seeMarks) {
        this.seeMarks = seeMarks;
    }

    public Integer getGraceMarks() {
        return graceMarks;
    }

    public void setGraceMarks(Integer graceMarks) {
        this.graceMarks = graceMarks;
    }
}