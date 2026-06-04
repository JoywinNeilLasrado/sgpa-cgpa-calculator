package com.gradecalculator.controller;

import com.gradecalculator.dto.response.DashboardResponse;
import com.gradecalculator.dto.response.SemesterResultRowResponse;
import com.gradecalculator.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/students/{id}/dashboard")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or @sec.isStudentOwner(principal, #id)")
    public ResponseEntity<DashboardResponse> getStudentDashboard(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.gradecalculator.security.UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(id));
    }

    @GetMapping("/results/student/{studentId}/semester/{semesterId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or @sec.isStudentOwner(principal, #studentId)")
    public ResponseEntity<List<SemesterResultRowResponse>> getSemesterResult(
            @PathVariable Long studentId,
            @PathVariable Long semesterId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.gradecalculator.security.UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getSemesterResult(studentId, semesterId));
    }
}
