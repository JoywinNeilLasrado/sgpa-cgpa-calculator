package com.gradecalculator.dto.response;

public record SemesterResultRowResponse(
        String courseCode,
        String courseName,
        Integer credits,
        String grade,
        int gradePoints,
        int creditPoints) {
}
