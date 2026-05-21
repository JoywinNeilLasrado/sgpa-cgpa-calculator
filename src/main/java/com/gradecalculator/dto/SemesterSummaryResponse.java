package com.gradecalculator.dto;

public record SemesterSummaryResponse(
        Long semesterId,
        Integer semesterNumber,
        double sgpa,
        int totalCredits) {
}
