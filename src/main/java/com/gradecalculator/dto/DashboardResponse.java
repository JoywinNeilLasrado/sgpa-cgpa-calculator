package com.gradecalculator.dto;

import java.util.List;

public record DashboardResponse(
        StudentSummaryResponse student,
        double overallCgpa,
        int totalCredits,
        List<SemesterSummaryResponse> semesters) {
}
