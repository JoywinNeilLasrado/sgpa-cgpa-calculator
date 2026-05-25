package com.gradecalculator.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CourseRequest {

    @NotBlank(message = "Course code cannot be blank")
    @Size(min = 2, max = 10, message = "Course code must be between 2 and 10 characters")
    private String courseCode;

    @NotBlank(message = "Course name cannot be blank")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String courseName;

    @NotNull(message = "Credits cannot be null")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 6, message = "Credits must be at most 6")
    private Integer credits;

    @NotNull(message = "Semester mapping is required")
    private Long semesterId;

    private com.gradecalculator.model.CourseType courseType = com.gradecalculator.model.CourseType.THEORY;

    private Long facultyId;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public com.gradecalculator.model.CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(com.gradecalculator.model.CourseType courseType) {
        this.courseType = courseType;
    }
}
