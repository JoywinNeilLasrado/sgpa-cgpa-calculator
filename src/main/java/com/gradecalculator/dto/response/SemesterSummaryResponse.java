package com.gradecalculator.dto.response;

public record SemesterSummaryResponse(
        Long semesterId,
        Integer semesterNumber,
        double sgpa,
        int totalCredits) {
}
