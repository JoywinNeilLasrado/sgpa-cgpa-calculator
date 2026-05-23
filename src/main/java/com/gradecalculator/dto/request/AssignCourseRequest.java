package com.gradecalculator.dto.request;

public record AssignCourseRequest(
    Long courseId,
    Long facultyId
) {}
