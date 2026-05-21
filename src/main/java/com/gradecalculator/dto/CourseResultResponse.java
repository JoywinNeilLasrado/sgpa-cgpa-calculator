package com.gradecalculator.dto;

/**
 * Individual course result for transcript.
 */
public class CourseResultResponse {
    
    private String courseCode;
    private String courseName;
    private int credits;
    private String grade;
    private int gradePoints;
    private int creditPoints;

    public CourseResultResponse() {}

    public CourseResultResponse(String courseCode, String courseName, int credits, String grade, int gradePoints, int creditPoints) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.grade = grade;
        this.gradePoints = gradePoints;
        this.creditPoints = creditPoints;
    }

    // Getters and Setters
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public int getGradePoints() { return gradePoints; }
    public void setGradePoints(int gradePoints) { this.gradePoints = gradePoints; }

    public int getCreditPoints() { return creditPoints; }
    public void setCreditPoints(int creditPoints) { this.creditPoints = creditPoints; }
}