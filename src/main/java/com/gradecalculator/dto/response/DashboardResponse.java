package com.gradecalculator.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive Dashboard Response for Analytics page.
 */
public class DashboardResponse {
    
    private Long studentId;
    private String studentName;
    private String studentRoll;
    private double cgpa;
    private int totalCredits;
    private int totalCourses;
    private int semestersCompleted;
    private Map<String, Double> sgpaBySemester;
    private Map<String, Integer> gradeDistribution;
    private int passRate;
    private int outstandingCount;
    private List<SemesterResultResponse> semesterResults;

    public DashboardResponse() {}

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentRoll() { return studentRoll; }
    public void setStudentRoll(String studentRoll) { this.studentRoll = studentRoll; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int totalCredits) { this.totalCredits = totalCredits; }

    public int getTotalCourses() { return totalCourses; }
    public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }

    public int getSemestersCompleted() { return semestersCompleted; }
    public void setSemestersCompleted(int semestersCompleted) { this.semestersCompleted = semestersCompleted; }

    public Map<String, Double> getSgpaBySemester() { return sgpaBySemester; }
    public void setSgpaBySemester(Map<String, Double> sgpaBySemester) { this.sgpaBySemester = sgpaBySemester; }

    public Map<String, Integer> getGradeDistribution() { return gradeDistribution; }
    public void setGradeDistribution(Map<String, Integer> gradeDistribution) { this.gradeDistribution = gradeDistribution; }

    public int getPassRate() { return passRate; }
    public void setPassRate(int passRate) { this.passRate = passRate; }

    public int getOutstandingCount() { return outstandingCount; }
    public void setOutstandingCount(int outstandingCount) { this.outstandingCount = outstandingCount; }

    public List<SemesterResultResponse> getSemesterResults() { return semesterResults; }
    public void setSemesterResults(List<SemesterResultResponse> semesterResults) { this.semesterResults = semesterResults; }
}
