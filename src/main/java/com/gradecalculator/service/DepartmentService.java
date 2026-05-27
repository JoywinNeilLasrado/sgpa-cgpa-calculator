package com.gradecalculator.service;

import com.gradecalculator.model.Department;
import com.gradecalculator.repository.DepartmentRepository;
import com.gradecalculator.exception.NotFoundException;
import com.gradecalculator.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    @Transactional
    public Department create(String name, String code) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Department name cannot be empty");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Department code cannot be empty");
        }
        
        String trimmedName = name.trim().toUpperCase();
        String trimmedCode = code.trim().toUpperCase();

        if (departmentRepository.findByName(trimmedName).isPresent()) {
            throw new ValidationException("Department with name '" + trimmedName + "' already exists");
        }
        if (departmentRepository.findByCode(trimmedCode).isPresent()) {
            throw new ValidationException("Department with code '" + trimmedCode + "' already exists");
        }

        return departmentRepository.save(new Department(trimmedName, trimmedCode));
    }

    @Transactional
    public Department update(Long id, String name, String code) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Department name cannot be empty");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Department code cannot be empty");
        }

        String trimmedName = name.trim().toUpperCase();
        String trimmedCode = code.trim().toUpperCase();

        departmentRepository.findByName(trimmedName).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new ValidationException("Another department with name '" + trimmedName + "' already exists");
            }
        });

        departmentRepository.findByCode(trimmedCode).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new ValidationException("Another department with code '" + trimmedCode + "' already exists");
            }
        });

        department.setName(trimmedName);
        department.setCode(trimmedCode);
        return departmentRepository.save(department);
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        departmentRepository.delete(department);
    }
}
