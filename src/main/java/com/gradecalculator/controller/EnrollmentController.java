package com.gradecalculator.controller;

import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.EnrollmentResponse;
import com.gradecalculator.service.EnrollmentService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.findAll());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.findByStudentId(studentId));
    }

    @GetMapping("/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudentAndSemester(
            @PathVariable Long studentId,
            @PathVariable Long semesterId) {
        return ResponseEntity.ok(enrollmentService.findByStudentIdAndSemesterId(studentId, semesterId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> createEnrollment(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
