package com.gradecalculator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Strongly-typed DTO representing a student creation/update request with validation rules.
 */
public class StudentRequest {

    @NotBlank(message = "Student name cannot be empty")
    @Size(min = 2, max = 100, message = "Student name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Student ID cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9-]{3,20}$", message = "Student ID must be between 3 and 20 alphanumeric characters")
    private String studentId;

    @NotBlank(message = "Branch cannot be empty")
    @Size(min = 2, max = 100, message = "Branch must be between 2 and 100 characters")
    private String branch;

    @NotBlank(message = "Date of birth cannot be empty")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dateOfBirth;

    public StudentRequest() {}

    public StudentRequest(String name, String studentId, String branch, String dateOfBirth) {
        this.name = name;
        this.studentId = studentId;
        this.branch = branch;
        this.dateOfBirth = dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
