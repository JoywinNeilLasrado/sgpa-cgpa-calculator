package com.gradecalculator.dto;

import com.gradecalculator.model.LetterGrade;

/**
 * Grade update request DTO
 */
public class GradeUpdateRequest {

    private Long enrollmentId;
    private LetterGrade grade;

    public GradeUpdateRequest() {}

    public GradeUpdateRequest(Long enrollmentId, LetterGrade grade) {
        this.enrollmentId = enrollmentId;
        this.grade = grade;
    }

    public Long getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Long enrollmentId) { this.enrollmentId = enrollmentId; }
    public LetterGrade getGrade() { return grade; }
    public void setGrade(LetterGrade grade) { this.grade = grade; }
}