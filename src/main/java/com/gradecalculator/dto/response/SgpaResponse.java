package com.gradecalculator.dto.response;

/**
 * DTO for SGPA response.
 */
public class SgpaResponse {

    private Long studentId;
    private Long semesterId;
    private double sgpa;
    private int totalCredits;
    private int totalCreditPoints;

    public SgpaResponse() {}

    public SgpaResponse(Long studentId, Long semesterId, double sgpa, int totalCredits, int totalCreditPoints) {
        this.studentId = studentId;
        this.semesterId = semesterId;
        this.sgpa = sgpa;
        this.totalCredits = totalCredits;
        this.totalCreditPoints = totalCreditPoints;
    }

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public double getSgpa() {
        return sgpa;
    }

    public void setSgpa(double sgpa) {
        this.sgpa = sgpa;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public int getTotalCreditPoints() {
        return totalCreditPoints;
    }

    public void setTotalCreditPoints(int totalCreditPoints) {
        this.totalCreditPoints = totalCreditPoints;
    }
}
