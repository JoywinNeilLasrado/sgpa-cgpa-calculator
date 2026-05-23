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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.demo.password:password123}")
    private String demoPassword;

    public StudentService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
                          UserService userService, com.gradecalculator.repository.AppUserRepository userRepository,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public Student create(String name, String rollNumber) {
        return create(name, rollNumber, "Computer Science", "2004-01-01");
    }

    public Student create(String name, String rollNumber, String branch) {
        return create(name, rollNumber, branch, "2004-01-01");
    }

    public Student create(String name, String rollNumber, String branch, String dateOfBirth) {
        validateStudent(name, rollNumber);
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        studentRepository.findByStudentId(rollNumber)
                .ifPresent(student -> {
                    throw new IllegalArgumentException("A student with this roll number already exists");
                });

        // Username is exactly the roll number in UPPER CASE!
        String username = rollNumber.trim().toUpperCase();

        Student student = new Student(name, rollNumber, branch);
        student.setUsername(username);
        student.setDateOfBirth(dateOfBirth.trim());
        Student savedStudent = studentRepository.save(student);

        // Register matching AppUser
        try {
            com.gradecalculator.dto.request.RegisterRequest regRequest = new com.gradecalculator.dto.request.RegisterRequest();
            regRequest.setUsername(username);
            regRequest.setPassword(dateOfBirth.trim()); // Initial password is Date of Birth!
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
        return update(id, name, rollNumber, branch, "2004-01-01");
    }

    public Student update(Long id, String name, String rollNumber, String branch, String dateOfBirth) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        validateStudent(name, rollNumber);
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        studentRepository.findByStudentId(rollNumber)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A student with this roll number already exists");
                });

        String oldUsername = student.getUsername();
        String newUsername = rollNumber.trim().toUpperCase();

        student.setName(name);
        student.setStudentId(rollNumber);
        student.setBranch(branch);
        student.setDateOfBirth(dateOfBirth.trim());
        student.setUsername(newUsername);

        // Sync AppUser profile
        if (oldUsername != null) {
            userRepository.findByUsername(oldUsername).ifPresent(user -> {
                user.setUsername(newUsername);
                user.setName(newUsername);
                user.setDepartment(branch);
                // Password is DOB
                user.setPassword(passwordEncoder.encode(dateOfBirth.trim()));
                userRepository.save(user);
            });
        }

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

    public Optional<Student> findByUsername(String username) {
        return studentRepository.findByUsername(username);
    }
}

