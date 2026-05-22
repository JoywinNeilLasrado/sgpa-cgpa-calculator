package com.gradecalculator.controller;

import com.gradecalculator.model.Department;
import com.gradecalculator.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartment(@PathVariable Long id) {
        return departmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Map<String, String> request) {
        validateDepartmentRequest(request);
        return ResponseEntity.ok(departmentService.create(request.get("name"), request.get("code")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        validateDepartmentRequest(request);
        return ResponseEntity.ok(departmentService.update(id, request.get("name"), request.get("code")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void validateDepartmentRequest(Map<String, String> request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        String name = request.get("name");
        String code = request.get("code");

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name cannot be empty");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Department code cannot be empty");
        }
    }
}
