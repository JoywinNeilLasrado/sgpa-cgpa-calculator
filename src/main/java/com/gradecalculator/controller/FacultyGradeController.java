package com.gradecalculator.controller;

import com.gradecalculator.dto.GradeUpdateRequest;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.repository.StudentRepository;
import com.gradecalculator.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Faculty Grade Management Controller
 */
@RestController
@RequestMapping("/api/faculty")
@Transactional
public class FacultyGradeController {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final EnrollmentService enrollmentService;

    public FacultyGradeController(EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository, CourseRepository courseRepository,
            SemesterRepository semesterRepository, EnrollmentService enrollmentService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Get all courses - GET /api/faculty/courses
     */
    @GetMapping("/courses")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    /**
     * Get all semesters - GET /api/faculty/semesters
     */
    @GetMapping("/semesters")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllSemesters() {
        return ResponseEntity.ok(semesterRepository.findAll());
    }

    /**
     * Get enrollments by course - GET /api/faculty/enrollments/course/{courseId}
     */
    @GetMapping("/enrollments/course/{courseId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getEnrollmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.findByCourseId(courseId));
    }

    /**
     * Get enrollments by semester - GET /api/faculty/enrollments/semester/{semesterId}
     */
    @GetMapping("/enrollments/semester/{semesterId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getEnrollmentsBySemester(@PathVariable Long semesterId) {
        return ResponseEntity.ok(enrollmentService.findBySemesterId(semesterId));
    }

    /**
     * Get student's enrollments - GET /api/faculty/enrollments/student/{studentId}
     */
    @GetMapping("/enrollments/student/{studentId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getStudentEnrollments(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.findByStudentId(studentId));
    }

    /**
     * Update grade - PUT /api/faculty/grades
     */
    @PutMapping("/grades")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> updateGrade(@RequestBody GradeUpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        enrollment.setGrade(request.getGrade());
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("grade", saved.getGrade());
        response.put("creditPoints", saved.getCreditPoints());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk update grades - PUT /api/faculty/grades/bulk
     */
    @PutMapping("/grades/bulk")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> bulkUpdateGrades(@RequestBody List<GradeUpdateRequest> requests) {
        List<Map<String, Object>> results = requests.stream().map(request -> {
            Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                    .orElse(null);
            
            if (enrollment != null) {
                enrollment.setGrade(request.getGrade());
                enrollmentRepository.save(enrollment);
                
                Map<String, Object> result = new HashMap<>();
                result.put("id", enrollment.getId());
                result.put("grade", enrollment.getGrade());
                return result;
            }
            return null;
        }).toList();

        return ResponseEntity.ok(results);
    }
}