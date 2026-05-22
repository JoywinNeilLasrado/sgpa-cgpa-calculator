package com.gradecalculator.controller;

import com.gradecalculator.dto.GradeUpdateRequest;
import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.dto.request.AssignCourseRequest;
import com.gradecalculator.service.UserService;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.model.Course;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.repository.StudentRepository;
import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AppUserRepository userRepository;
    private final UserService userService;

    public FacultyGradeController(EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository, CourseRepository courseRepository,
            SemesterRepository semesterRepository, EnrollmentService enrollmentService,
            AppUserRepository userRepository, UserService userService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
        this.userService = userService;
    }


    /**
     * Get all faculty users - GET /api/faculty/members
     */
    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllFaculty() {
        return ResponseEntity.ok(userRepository.findByRole(AppUser.Role.FACULTY));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerFaculty(@RequestBody RegisterRequest request) {
        request.setRole(com.gradecalculator.model.AppUser.Role.FACULTY);
        com.gradecalculator.model.AppUser faculty = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(faculty);
    }

    @DeleteMapping("/members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFaculty(@PathVariable Long id) {
        com.gradecalculator.model.AppUser faculty = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found"));
        
        // Nullify faculty in assigned courses
        List<Course> courses = courseRepository.findByFacultyId(id);
        for (Course course : courses) {
            course.setFaculty(null);
            courseRepository.save(course);
        }
        
        userRepository.delete(faculty);
        return ResponseEntity.ok("Faculty member deleted");
    }


    // New endpoint to assign a faculty to a course
    @PostMapping("/assign-course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignFacultyToCourse(@RequestBody com.gradecalculator.dto.request.AssignCourseRequest request) {
        // Fetch course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        // Fetch faculty
        com.gradecalculator.model.AppUser faculty = userRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
        // Assign and save
        course.setFaculty(faculty);
        courseRepository.save(course);
        return ResponseEntity.ok("Faculty assigned to course");
    }


    /**
     * Get all courses - GET /api/faculty/courses
     */
    @GetMapping("/courses")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllCourses(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getRole().equals("FACULTY")) {
            return ResponseEntity.ok(courseRepository.findByFacultyId(principal.getId()));
        }
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
    public ResponseEntity<?> getEnrollmentsByCourse(@PathVariable Long courseId, @AuthenticationPrincipal UserPrincipal principal) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (principal.getRole().equals("FACULTY") && (course.getFaculty() == null || !course.getFaculty().getId().equals(principal.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You are not assigned to this course.");
        }
        return ResponseEntity.ok(enrollmentService.findByCourseId(courseId));
    }

    /**
     * Get enrollments by semester - GET /api/faculty/enrollments/semester/{semesterId}
     */
    @GetMapping("/enrollments/semester/{semesterId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getEnrollmentsBySemester(@PathVariable Long semesterId, @AuthenticationPrincipal UserPrincipal principal) {
        List<com.gradecalculator.dto.EnrollmentResponse> enrollments = enrollmentService.findBySemesterId(semesterId);
        if (principal.getRole().equals("FACULTY")) {
            enrollments = enrollments.stream()
                .filter(e -> {
                    Course course = courseRepository.findById(e.courseId()).orElse(null);
                    return course != null && course.getFaculty() != null && course.getFaculty().getId().equals(principal.getId());
                }).toList();
        }
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get student's enrollments - GET /api/faculty/enrollments/student/{studentId}
     */
    @GetMapping("/enrollments/student/{studentId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getStudentEnrollments(@PathVariable Long studentId, @AuthenticationPrincipal UserPrincipal principal) {
        List<com.gradecalculator.dto.EnrollmentResponse> enrollments = enrollmentService.findByStudentId(studentId);
        if (principal.getRole().equals("FACULTY")) {
            enrollments = enrollments.stream()
                .filter(e -> {
                    Course course = courseRepository.findById(e.courseId()).orElse(null);
                    return course != null && course.getFaculty() != null && course.getFaculty().getId().equals(principal.getId());
                }).toList();
        }
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Update grade - PUT /api/faculty/grades
     */
    @PutMapping("/grades")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> updateGrade(@RequestBody GradeUpdateRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        if (principal.getRole().equals("FACULTY")) {
            Course course = enrollment.getCourse();
            if (course == null || course.getFaculty() == null || !course.getFaculty().getId().equals(principal.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You are not assigned to this course.");
            }
        }

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
    public ResponseEntity<?> bulkUpdateGrades(@RequestBody List<GradeUpdateRequest> requests, @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getRole().equals("FACULTY")) {
            for (GradeUpdateRequest request : requests) {
                Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + request.getEnrollmentId()));
                Course course = enrollment.getCourse();
                if (course == null || course.getFaculty() == null || !course.getFaculty().getId().equals(principal.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You are not assigned to course for enrollment " + request.getEnrollmentId());
                }
            }
        }

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