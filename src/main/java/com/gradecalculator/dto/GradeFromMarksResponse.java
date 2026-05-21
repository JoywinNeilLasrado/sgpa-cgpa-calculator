package com.gradecalculator.dto;

public record GradeFromMarksResponse(
        int marks,
        String grade,
        String performance,
        int points) {
}
