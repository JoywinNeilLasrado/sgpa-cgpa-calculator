package com.gradecalculator.dto;

import com.gradecalculator.model.LetterGrade;

public record GradeUpdateRequest(
    Long enrollmentId,
    LetterGrade grade,
    Integer cieMarks,
    Integer cieTheoryMarks,
    Integer cieLabMarks,
    Integer test1Marks,
    Integer test2Marks,
    Integer assignmentMarks,
    Integer oaaMarks,
    Integer regularLabMarks,
    Integer labTestMarks,
    Integer labRecordMarks,
    Integer seeMarks,
    Integer graceMarks
) {}