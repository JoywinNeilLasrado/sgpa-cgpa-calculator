package com.gradecalculator.controller;

import com.gradecalculator.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/students/{id}/dashboard")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(id));
    }

    @GetMapping("/results/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<List<Map<String, Object>>> getSemesterResult(
            @PathVariable Long studentId,
            @PathVariable Long semesterId) {
        return ResponseEntity.ok(dashboardService.getSemesterResult(studentId, semesterId));
    }
}
