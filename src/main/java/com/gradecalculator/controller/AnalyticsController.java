package com.gradecalculator.controller;

import com.gradecalculator.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Analytics Controller
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get SGPA trends for charts
     */
    @GetMapping("/sgpa-trends/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getSGPATrends(@PathVariable Long studentId) {
        return ResponseEntity.ok(analyticsService.getSGPATrends(studentId));
    }

    /**
     * Get class rankings
     */
    @GetMapping("/rankings")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRankings(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getRankings(limit));
    }

    /**
     * Get toppers
     */
    @GetMapping("/toppers")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getToppers(
            @RequestParam(defaultValue = "5") int count) {
        return ResponseEntity.ok(analyticsService.getToppers(count));
    }

    /**
     * Get course analytics
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourseAnalytics(@PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getCourseAnalytics(courseId));
    }

    /**
     * Get class statistics
     */
    @GetMapping("/class")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getClassStatistics() {
        return ResponseEntity.ok(analyticsService.getClassStatistics());
    }
}