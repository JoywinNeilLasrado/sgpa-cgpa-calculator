package com.gradecalculator.controller.v2;

import com.gradecalculator.dto.request.StudentRequest;
import com.gradecalculator.dto.response.ApiResponse;
import com.gradecalculator.model.Student;
import com.gradecalculator.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API v2 for Student management.
 * <p>
 * Version 2 introduces:
 * <ul>
 *   <li>Consistent error codes with {@code STUDENT_*} prefix</li>
 *   <li>Pagination support for all list endpoints</li>
 *   <li>Enhanced API documentation with OpenAPI</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v2/students")
@Tag(name = "Students v2", description = "Student management API v2 - enhanced with pagination and better error handling")
public class StudentControllerV2 {

    private final StudentService studentService;

    public StudentControllerV2(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Get all students", description = "Returns paginated list of students")
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<Student> students = studentService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<ApiResponse<Student>> getStudentById(
            @Parameter(description = "Student ID") @PathVariable Long id) {
        return studentService.findById(id)
                .map(student -> ResponseEntity.ok(ApiResponse.success(student)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new student")
    public ResponseEntity<ApiResponse<Student>> createStudent(
            @Valid @RequestBody StudentRequest request) {
        Student student = studentService.create(
                request.getName(),
                request.getStudentId(),
                request.getBranch(),
                request.getDateOfBirth()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student created successfully", student));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing student")
    public ResponseEntity<ApiResponse<Student>> updateStudent(
            @Parameter(description = "Student ID") @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        Student updated = studentService.update(
                id,
                request.getName(),
                request.getStudentId(),
                request.getBranch(),
                request.getDateOfBirth()
        );
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @Parameter(description = "Student ID") @PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search student by username")
    public ResponseEntity<ApiResponse<Student>> searchByUsername(
            @Parameter(description = "Username (roll number)") @RequestParam String username) {
        return studentService.findByUsername(username)
                .map(student -> ResponseEntity.ok(ApiResponse.success(student)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}