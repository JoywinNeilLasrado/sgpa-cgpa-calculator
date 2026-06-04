package com.gradecalculator.dto;

/**
 * Semester result for transcript display.
 */
public class SemesterResultResponse {
    
    private Long semesterId;
    private Integer semesterNumber;
    private double sgpa;
    private int totalCredits;
    private int totalPoints;
    private java.util.List<CourseResultResponse> courses;

    public SemesterResultResponse() {}

    public SemesterResultResponse(Integer semesterNumber, double sgpa, int totalCredits, int totalPoints) {
        this.semesterNumber = semesterNumber;
        this.sgpa = sgpa;
        this.totalCredits = totalCredits;
        this.totalPoints = totalPoints;
    }

    // Getters and Setters
    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long semesterId) { this.semesterId = semesterId; }

    public Integer getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(Integer semesterNumber) { this.semesterNumber = semesterNumber; }

    public double getSgpa() { return sgpa; }
    public void setSgpa(double sgpa) { this.sgpa = sgpa; }

    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int totalCredits) { this.totalCredits = totalCredits; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public java.util.List<CourseResultResponse> getCourses() { return courses; }
    public void setCourses(java.util.List<CourseResultResponse> courses) { this.courses = courses; }
}