package com.gradecalculator.dto;

/**
 * DTO for CGPA response.
 */
public class CgpaResponse {

    private Long studentId;
    private double cgpa;
    private int totalEarnedCredits;
    private int totalCreditPoints;
    private int semestersCompleted;

    public CgpaResponse() {}

    public CgpaResponse(Long studentId, double cgpa, int totalEarnedCredits, int totalCreditPoints, int semestersCompleted) {
        this.studentId = studentId;
        this.cgpa = cgpa;
        this.totalEarnedCredits = totalEarnedCredits;
        this.totalCreditPoints = totalCreditPoints;
        this.semestersCompleted = semestersCompleted;
    }

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public int getTotalEarnedCredits() {
        return totalEarnedCredits;
    }

    public void setTotalEarnedCredits(int totalEarnedCredits) {
        this.totalEarnedCredits = totalEarnedCredits;
    }

    public int getTotalCreditPoints() {
        return totalCreditPoints;
    }

    public void setTotalCreditPoints(int totalCreditPoints) {
        this.totalCreditPoints = totalCreditPoints;
    }

    public int getSemestersCompleted() {
        return semestersCompleted;
    }

    public void setSemestersCompleted(int semestersCompleted) {
        this.semestersCompleted = semestersCompleted;
    }
}