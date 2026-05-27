package com.gradecalculator.service;

import com.gradecalculator.model.Student;
import com.gradecalculator.util.ValidationUtil;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gradecalculator.exception.NotFoundException;
import com.gradecalculator.exception.ValidationException;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing student registries, profiles, and associated user accounts.
 */
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

    /**
     * Retrieves all student records registered in the system.
     *
     * @return list of student records
     */
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    /**
     * Retrieves a page of student records registered in the system.
     *
     * @param pageable pagination parameters
     * @return Page of student records
     */
    public org.springframework.data.domain.Page<Student> findAll(org.springframework.data.domain.Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    /**
     * Finds a student by their unique database identifier.
     *
     * @param id the unique student ID
     * @return Optional containing the student if found
     */
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    /**
     * Creates a new student profile with default branch and DOB parameters.
     *
     * @param name       the student full name
     * @param rollNumber the student registry roll number
     * @return the saved Student entity
     */
    public Student create(String name, String rollNumber) {
        return create(name, rollNumber, "Computer Science", "2004-01-01");
    }

    /**
     * Creates a new student profile with a specified branch.
     *
     * @param name       the student full name
     * @param rollNumber the student registry roll number
     * @param branch     the academic department branch
     * @return the saved Student entity
     */
    public Student create(String name, String rollNumber, String branch) {
        return create(name, rollNumber, branch, "2004-01-01");
    }

    /**
     * Creates a new student profile with full parameters and registers a corresponding user credential.
     *
     * @param name        the student full name
     * @param rollNumber  the student roll number
     * @param branch      the academic department branch
     * @param dateOfBirth the student date of birth (initial password)
     * @return the saved Student entity
     */
    public Student create(String name, String rollNumber, String branch, String dateOfBirth) {
        validateStudent(name, rollNumber);
        ValidationUtil.requireNonBlank(dateOfBirth, "Date of birth is required");
        studentRepository.findByStudentId(rollNumber)
                .ifPresent(student -> {
                    throw new ValidationException("A student with this roll number already exists");
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

    /**
     * Updates an existing student record with default academic branch and date of birth.
     *
     * @param id         the database identifier of the student
     * @param name       the updated name of the student
     * @param rollNumber the updated roll number of the student
     * @return the updated student record
     */
    public Student update(Long id, String name, String rollNumber) {
        return update(id, name, rollNumber, "Computer Science");
    }

    /**
     * Updates an existing student record with a specified department branch and default date of birth.
     *
     * @param id         the database identifier of the student
     * @param name       the updated name of the student
     * @param rollNumber the updated roll number of the student
     * @param branch     the updated department branch of the student
     * @return the updated student record
     */
    public Student update(Long id, String name, String rollNumber, String branch) {
        return update(id, name, rollNumber, branch, "2004-01-01");
    }

    /**
     * Updates an existing student record with complete fields and synchronizes user credentials.
     *
     * @param id          the database identifier of the student
     * @param name        the updated name of the student
     * @param rollNumber  the updated roll number of the student
     * @param branch      the updated department branch of the student
     * @param dateOfBirth the updated date of birth of the student (also updates default login credential if changed)
     * @return the updated student record
     */
    public Student update(Long id, String name, String rollNumber, String branch, String dateOfBirth) {
        ValidationUtil.requireNonNull(id, "Student ID cannot be null");
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        validateStudent(name, rollNumber);
        ValidationUtil.requireNonBlank(dateOfBirth, "Date of birth is required");
        studentRepository.findByStudentId(rollNumber)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ValidationException("A student with this roll number already exists");
                });

        String oldUsername = student.getUsername();
        String newUsername = rollNumber.trim().toUpperCase();
        String oldDob = student.getDateOfBirth();

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
                // Only update/reset password if the DOB actually changed
                if (oldDob == null || !oldDob.equals(dateOfBirth.trim())) {
                    user.setPassword(passwordEncoder.encode(dateOfBirth.trim()));
                    user.setMustChangePassword(true);
                }
                userRepository.save(user);
            });
        }

        return studentRepository.save(student);
    }

    /**
     * Deletes a student from the registry, cleans up their course enrollments, and deletes the matching user credential.
     *
     * @param id the database identifier of the student to delete
     */
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new ValidationException("Student ID cannot be null");
        }
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
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
            throw new ValidationException(message);
        }
    }

    /**
     * Finds a student record by their matching user login username.
     *
     * @param username the username associated with the student profile
     * @return Optional containing the student if found
     */
    public Optional<Student> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        return studentRepository.findByUsername(username);
    }
}

