package com.gradecalculator.dto;

import java.util.Map;

public record GradeScaleResponse(Map<String, GradeScaleEntryResponse> grades) {
}
