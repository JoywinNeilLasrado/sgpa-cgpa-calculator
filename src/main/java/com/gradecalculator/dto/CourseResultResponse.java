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

    private String courseType;
    private Integer test1Marks;
    private Integer test2Marks;
    private Integer assignmentMarks;
    private Integer oaaMarks;
    private Integer regularLabMarks;
    private Integer labTestMarks;
    private Integer labRecordMarks;
    private Integer seeMarks;
    private Integer graceMarks;
    private Integer totalMarks;

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

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public Integer getTest1Marks() { return test1Marks; }
    public void setTest1Marks(Integer test1Marks) { this.test1Marks = test1Marks; }

    public Integer getTest2Marks() { return test2Marks; }
    public void setTest2Marks(Integer test2Marks) { this.test2Marks = test2Marks; }

    public Integer getAssignmentMarks() { return assignmentMarks; }
    public void setAssignmentMarks(Integer assignmentMarks) { this.assignmentMarks = assignmentMarks; }

    public Integer getOaaMarks() { return oaaMarks; }
    public void setOaaMarks(Integer oaaMarks) { this.oaaMarks = oaaMarks; }

    public Integer getRegularLabMarks() { return regularLabMarks; }
    public void setRegularLabMarks(Integer regularLabMarks) { this.regularLabMarks = regularLabMarks; }

    public Integer getLabTestMarks() { return labTestMarks; }
    public void setLabTestMarks(Integer labTestMarks) { this.labTestMarks = labTestMarks; }

    public Integer getLabRecordMarks() { return labRecordMarks; }
    public void setLabRecordMarks(Integer labRecordMarks) { this.labRecordMarks = labRecordMarks; }

    public Integer getSeeMarks() { return seeMarks; }
    public void setSeeMarks(Integer seeMarks) { this.seeMarks = seeMarks; }

    public Integer getGraceMarks() { return graceMarks; }
    public void setGraceMarks(Integer graceMarks) { this.graceMarks = graceMarks; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
}