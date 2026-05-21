package com.gradecalculator.dto;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        String studentRollNumber,
        Long courseId,
        String courseCode,
        String courseName,
        Integer credits,
        Long semesterId,
        Integer semesterNumber,
        String grade,
        int gradePoints,
        int creditPoints) {
}
