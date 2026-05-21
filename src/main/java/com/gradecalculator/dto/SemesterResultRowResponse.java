package com.gradecalculator.dto;

public record SemesterResultRowResponse(
        String courseCode,
        String courseName,
        Integer credits,
        String grade,
        int gradePoints,
        int creditPoints) {
}
