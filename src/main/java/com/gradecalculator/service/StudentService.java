package com.gradecalculator.service;

import com.gradecalculator.model.Student;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final com.gradecalculator.repository.AppUserRepository userRepository;

    public StudentService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
                          UserService userService, com.gradecalculator.repository.AppUserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public Student create(String name, String rollNumber) {
        return create(name, rollNumber, "Computer Science");
    }

    public Student create(String name, String rollNumber, String branch) {
        validateStudent(name, rollNumber);
        studentRepository.findByStudentId(rollNumber)
                .ifPresent(student -> {
                    throw new IllegalArgumentException("A student with this roll number already exists");
                });

        // Generate username (lowercase first name, fallback to studentId if duplicate)
        String username = name.split(" ")[0].toLowerCase();
        if (userRepository.existsByUsername(username)) {
            username = rollNumber.toLowerCase();
        }

        Student student = new Student(name, rollNumber, branch);
        student.setUsername(username);
        Student savedStudent = studentRepository.save(student);

        // Register matching AppUser
        try {
            com.gradecalculator.dto.request.RegisterRequest regRequest = new com.gradecalculator.dto.request.RegisterRequest();
            regRequest.setUsername(username);
            regRequest.setPassword("password123");
            regRequest.setRole(com.gradecalculator.model.AppUser.Role.STUDENT);
            userService.register(regRequest);
        } catch (Exception e) {
            // Ignore or log if already exists
        }

        return savedStudent;
    }

    public Student update(Long id, String name, String rollNumber) {
        return update(id, name, rollNumber, "Computer Science");
    }

    public Student update(Long id, String name, String rollNumber, String branch) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        validateStudent(name, rollNumber);
        studentRepository.findByStudentId(rollNumber)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A student with this roll number already exists");
                });

        student.setName(name);
        student.setStudentId(rollNumber);
        student.setBranch(branch);
        return studentRepository.save(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student.getUsername() != null) {
            userRepository.findByUsername(student.getUsername())
                    .ifPresent(userRepository::delete);
        }
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudentId(id));
        studentRepository.delete(student);
    }

    private void validateStudent(String name, String rollNumber) {
        validateText(name, "Student name is required");
        validateText(rollNumber, "Roll number is required");
    }

    private void validateText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
