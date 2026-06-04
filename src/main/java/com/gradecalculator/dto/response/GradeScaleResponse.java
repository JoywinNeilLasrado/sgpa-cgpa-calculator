package com.gradecalculator.dto.response;

import java.util.Map;

public record GradeScaleResponse(Map<String, GradeScaleEntryResponse> grades) {
}
