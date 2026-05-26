package com.gradecalculator.controller;

import com.gradecalculator.model.Semester;
import com.gradecalculator.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Semester>> getAllSemesters() {
        return ResponseEntity.ok(semesterService.findAll());
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<Semester> getSemester(@PathVariable Long id) {
        return semesterService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Semester> createSemester(@RequestBody Map<String, Integer> request) {
        validateSemesterRequest(request);
        return ResponseEntity.ok(semesterService.create(request.get("semesterNumber")));
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Semester> updateSemester(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        validateSemesterRequest(request);
        return ResponseEntity.ok(semesterService.update(id, request.get("semesterNumber")));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSemester(@PathVariable Long id) {
        semesterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void validateSemesterRequest(Map<String, Integer> request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        Integer semesterNumber = request.get("semesterNumber");
        if (semesterNumber == null) {
            throw new IllegalArgumentException("Semester number is required");
        }
        if (semesterNumber < 1 || semesterNumber > 10) {
            throw new IllegalArgumentException("Semester number must be between 1 and 10");
        }
    }
}
