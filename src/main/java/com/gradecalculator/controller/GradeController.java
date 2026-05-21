package com.gradecalculator.controller;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.service.GradeCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST endpoints for grade calculations and grade scale reference data.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GradeController {

    private final GradeCalculationService gradeCalculationService;

    public GradeController(GradeCalculationService gradeCalculationService) {
        this.gradeCalculationService = gradeCalculationService;
    }

    @GetMapping("/sgpa/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<SgpaResponse> calculateSGPA(
            @PathVariable Long studentId,
            @PathVariable Long semesterId) {
        return ResponseEntity.ok(gradeCalculationService.calculateSGPA(studentId, semesterId));
    }

    @GetMapping("/cgpa/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<CgpaResponse> calculateCGPA(
            @PathVariable Long studentId,
            @PathVariable Long semesterId) {
        return ResponseEntity.ok(gradeCalculationService.calculateCGPA(studentId, semesterId));
    }

    @GetMapping("/cgpa/student/{studentId}")
    public ResponseEntity<CgpaResponse> calculateOverallCGPA(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeCalculationService.calculateOverallCGPA(studentId));
    }

    @GetMapping("/grade-scale")
    public ResponseEntity<Map<String, Object>> getGradeScale() {
        return ResponseEntity.ok(Map.of(
                "grades", Map.of(
                        "O", Map.of("performance", "Outstanding", "marks", "90-100", "points", 10),
                        "A+", Map.of("performance", "Excellent", "marks", "80-89", "points", 9),
                        "A", Map.of("performance", "Very Good", "marks", "70-79", "points", 8),
                        "B+", Map.of("performance", "Good", "marks", "60-69", "points", 7),
                        "B", Map.of("performance", "Above Average", "marks", "55-59", "points", 6),
                        "C", Map.of("performance", "Average", "marks", "50-54", "points", 5),
                        "P", Map.of("performance", "Pass", "marks", "40-49", "points", 4),
                        "F", Map.of("performance", "Fail", "marks", "00-39", "points", 0)
                )
        ));
    }
}
