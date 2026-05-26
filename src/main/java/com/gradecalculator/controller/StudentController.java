package com.gradecalculator.controller;

import com.gradecalculator.model.Student;
import com.gradecalculator.service.StudentService;
import com.gradecalculator.dto.StudentRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Student profiles and credentials.
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Retrieves all registered students in the system.
     *
     * @return List of student records
     */
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.findAll());
    }

    /**
     * Retrieves a specific student record by its database identifier.
     *
     * @param id        the unique identifier of the student
     * @param principal the authenticated user context
     * @return Student record or a 404 Not Found response
     */
    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or @sec.isStudentOwner(principal, #id)")
    public ResponseEntity<Student> getStudent(@PathVariable Long id, @org.springframework.security.core.annotation.AuthenticationPrincipal com.gradecalculator.security.UserPrincipal principal) {
        return studentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates and registers a new student profile in the system.
     *
     * @param request strongly-typed validated StudentRequest payload
     * @return the saved Student entity
     */
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> createStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.create(
                request.getName(), 
                request.getStudentId(), 
                request.getBranch(), 
                request.getDateOfBirth()
        ));
    }

    /**
     * Updates an existing student profile by database identifier.
     *
     * @param id      the unique identifier of the student
     * @param request strongly-typed validated StudentRequest updates payload
     * @return the updated Student entity
     */
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.update(
                id, 
                request.getName(), 
                request.getStudentId(), 
                request.getBranch(), 
                request.getDateOfBirth()
        ));
    }

    /**
     * Deletes and purges a student profile by database identifier.
     *
     * @param id the unique identifier of the student to delete
     * @return 204 No Content response
     */
    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
