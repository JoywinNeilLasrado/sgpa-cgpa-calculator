package com.gradecalculator.dto;

import com.gradecalculator.model.LetterGrade;

public record GradeUpdateRequest(
    Long enrollmentId,
    LetterGrade grade
) {}