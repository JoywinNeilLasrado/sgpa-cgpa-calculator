package com.gradecalculator.dto.response;

public record GradeFromMarksResponse(
        int marks,
        String grade,
        String performance,
        int points) {
}
