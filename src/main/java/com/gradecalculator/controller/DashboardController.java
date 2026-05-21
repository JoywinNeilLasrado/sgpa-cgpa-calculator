package com.gradecalculator.controller;

import com.gradecalculator.dto.DashboardResponse;
import com.gradecalculator.dto.SemesterResultRowResponse;
import com.gradecalculator.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/students/{id}/dashboard")
    public ResponseEntity<DashboardResponse> getStudentDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(id));
    }

    @GetMapping("/results/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<List<SemesterResultRowResponse>> getSemesterResult(
            @PathVariable Long studentId,
            @PathVariable Long semesterId) {
        return ResponseEntity.ok(dashboardService.getSemesterResult(studentId, semesterId));
    }
}
