package com.gradecalculator.controller;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.GradeFromMarksResponse;
import com.gradecalculator.dto.GradeScaleEntryResponse;
import com.gradecalculator.dto.GradeScaleResponse;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.service.GradeCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST endpoints for grade calculations and grade scale reference data.
 */
@RestController
@RequestMapping("/api")
public class GradeController {

    private final GradeCalculationService gradeCalculationService;

    public GradeController(GradeCalculationService gradeCalculationService) {
        this.gradeCalculationService = gradeCalculationService;
    }

    @GetMapping("/sgpa/student/{studentId}/semester/{semesterId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or @sec.isStudentOwner(principal, #studentId)")
    public ResponseEntity<SgpaResponse> calculateSGPA(
            @PathVariable Long studentId,
            @PathVariable Long semesterId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.gradecalculator.security.UserPrincipal principal) {
        return ResponseEntity.ok(gradeCalculationService.calculateSGPA(studentId, semesterId));
    }

    @GetMapping("/cgpa/student/{studentId}/semester/{semesterId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or @sec.isStudentOwner(principal, #studentId)")
    public ResponseEntity<CgpaResponse> calculateCGPA(
            @PathVariable Long studentId,
            @PathVariable Long semesterId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.gradecalculator.security.UserPrincipal principal) {
        return ResponseEntity.ok(gradeCalculationService.calculateCGPA(studentId, semesterId));
    }

    @GetMapping("/cgpa/student/{studentId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or @sec.isStudentOwner(principal, #studentId)")
    public ResponseEntity<CgpaResponse> calculateOverallCGPA(
            @PathVariable Long studentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.gradecalculator.security.UserPrincipal principal) {
        return ResponseEntity.ok(gradeCalculationService.calculateOverallCGPA(studentId));
    }

    @GetMapping("/grade-scale")
    public ResponseEntity<GradeScaleResponse> getGradeScale() {
        return ResponseEntity.ok(new GradeScaleResponse(Map.of(
                "O", new GradeScaleEntryResponse("Outstanding", "90-100", 10),
                "A+", new GradeScaleEntryResponse("Excellent", "80-89", 9),
                "A", new GradeScaleEntryResponse("Very Good", "70-79", 8),
                "B+", new GradeScaleEntryResponse("Good", "60-69", 7),
                "B", new GradeScaleEntryResponse("Above Average", "55-59", 6),
                "C", new GradeScaleEntryResponse("Average", "50-54", 5),
                "P", new GradeScaleEntryResponse("Pass", "40-49", 4),
                "F", new GradeScaleEntryResponse("Fail", "00-39", 0)
        )));
    }

    @GetMapping("/grades/from-marks")
    public ResponseEntity<GradeFromMarksResponse> getGradeFromMarks(@RequestParam int marks) {
        LetterGrade grade = LetterGrade.fromMarks(marks);
        return ResponseEntity.ok(new GradeFromMarksResponse(
                marks,
                grade.getGrade(),
                grade.getPerformanceLevel(),
                grade.getGradePoints()
        ));
    }
}
