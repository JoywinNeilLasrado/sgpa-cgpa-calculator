package com.gradecalculator.controller;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.*;
import com.gradecalculator.service.GradeCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing students, courses, enrollments, and calculating SGPA/CGPA.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GradeController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeCalculationService gradeCalculationService;

    // ============ Student Endpoints ============

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@RequestBody Map<String, String> request) {
        Student student = new Student(request.get("name"), request.get("studentId"));
        return ResponseEntity.ok(studentRepository.save(student));
    }

    // ============ Semester Endpoints ============

    @GetMapping("/semesters")
    public ResponseEntity<List<Semester>> getAllSemesters() {
        return ResponseEntity.ok(semesterRepository.findAll());
    }

    @GetMapping("/semesters/{id}")
    public ResponseEntity<Semester> getSemester(@PathVariable Long id) {
        return semesterRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/semesters")
    public ResponseEntity<Semester> createSemester(@RequestBody Map<String, Integer> request) {
        Semester semester = new Semester(request.get("semesterNumber"));
        return ResponseEntity.ok(semesterRepository.save(semester));
    }

    // ============ Course Endpoints ============

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @GetMapping("/courses/semester/{semesterId}")
    public ResponseEntity<List<Course>> getCoursesBySemester(@PathVariable Long semesterId) {
        return ResponseEntity.ok(courseRepository.findBySemesterId(semesterId));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Map<String, Object> request) {
        Long semesterId = Long.parseLong(request.get("semesterId").toString());
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

        Course course = new Course(
                request.get("courseCode").toString(),
                request.get("courseName").toString(),
                Integer.parseInt(request.get("credits").toString())
        );
        course.setSemester(semester);
        return ResponseEntity.ok(courseRepository.save(course));
    }

    // ============ Enrollment Endpoints ============

    @GetMapping("/enrollments/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentRepository.findByStudentId(studentId));
    }

    @GetMapping("/enrollments/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudentAndSemester(
            @PathVariable Long studentId, 
            @PathVariable Long semesterId) {
        return ResponseEntity.ok(enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId));
    }

    @PostMapping("/enrollments")
    public ResponseEntity<Enrollment> createEnrollment(@RequestBody EnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        LetterGrade grade = LetterGrade.fromGrade(request.getGrade());
        
        Enrollment enrollment = new Enrollment(student, course, grade);
        return ResponseEntity.ok(enrollmentRepository.save(enrollment));
    }

    // ============ SGPA and CGPA Calculation Endpoints ============

    /**
     * Calculate SGPA for a student in a specific semester.
     * 
     * Formula: SGPA = Σ(Course Credits × Grade Points) / Σ(Course Credits)
     */
    @GetMapping("/sgpa/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<SgpaResponse> calculateSGPA(
            @PathVariable Long studentId, 
            @PathVariable Long semesterId) {
        try {
            SgpaResponse response = gradeCalculationService.calculateSGPA(studentId, semesterId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calculate CGPA for a student up to a specific semester.
     * 
     * Formula: CGPA = Σ(Credit Points excluding F grades) / Σ(Course Credits excluding F grades)
     */
    @GetMapping("/cgpa/student/{studentId}/semester/{semesterId}")
    public ResponseEntity<CgpaResponse> calculateCGPA(
            @PathVariable Long studentId, 
            @PathVariable Long semesterId) {
        try {
            CgpaResponse response = gradeCalculationService.calculateCGPA(studentId, semesterId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calculate overall CGPA for a student across all semesters.
     */
    @GetMapping("/cgpa/student/{studentId}")
    public ResponseEntity<CgpaResponse> calculateOverallCGPA(@PathVariable Long studentId) {
        try {
            CgpaResponse response = gradeCalculationService.calculateOverallCGPA(studentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ============ Grade Scale Info ============

    /**
     * Get the grade points for each letter grade.
     */
    @GetMapping("/grade-scale")
    public ResponseEntity<Map<String, Object>> getGradeScale() {
        return ResponseEntity.ok(Map.of(
                "grades", Map.of(
                        "O", Map.of("performance", "Outstanding", "marks", "90-100", "points", 10),
                        "A+", Map.of("performance", "Excellent", "marks", "80-89", "points", 9),
                        "A", Map.of("performance", "Very Good", "marks", "70-79", "points", 8),
                        "B+", Map.of("performance", "Good", "marks", "60-69", "points", 7),
                        "B", Map.of("performance", "Above Average", "marks", "55-59", "points", 6),
                        "C", Map.of("performance", "Average", "marks", "50-54", "points", 5),
                        "P", Map.of("performance", "Pass", "marks", "40-49", "points", 4),
                        "F", Map.of("performance", "Fail", "marks", "00-39", "points", 0)
                )
        ));
    }
}