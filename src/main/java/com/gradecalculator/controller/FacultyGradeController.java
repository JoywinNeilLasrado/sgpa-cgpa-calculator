package com.gradecalculator.controller;

import com.gradecalculator.dto.GradeUpdateRequest;
import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.dto.request.AssignCourseRequest;
import com.gradecalculator.service.UserService;
import com.gradecalculator.service.FacultyService;
import com.gradecalculator.service.CourseService;
import com.gradecalculator.service.SemesterService;
import com.gradecalculator.service.EnrollmentService;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.Course;
import com.gradecalculator.security.UserPrincipal;
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

    private final FacultyService facultyService;
    private final CourseService courseService;
    private final SemesterService semesterService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    public FacultyGradeController(FacultyService facultyService,
            CourseService courseService, SemesterService semesterService,
            EnrollmentService enrollmentService, UserService userService) {
        this.facultyService = facultyService;
        this.courseService = courseService;
        this.semesterService = semesterService;
        this.enrollmentService = enrollmentService;
        this.userService = userService;
    }


    /**
     * Get all faculty users - GET /api/faculty/members
     */
    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllFaculty() {
        return ResponseEntity.ok(facultyService.findAllFacultyMembers());
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
        facultyService.deleteFacultyMember(id);
        return ResponseEntity.ok("Faculty member deleted");
    }

    @PutMapping("/members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateFaculty(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String username = request.get("username");
        String email = request.get("email");
        String department = request.get("department");
        return ResponseEntity.ok(facultyService.updateFacultyMember(id, name, username, email, department));
    }


    // New endpoint to assign a faculty to a course
    @PostMapping("/assign-course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignFacultyToCourse(@RequestBody AssignCourseRequest request) {
        facultyService.assignFacultyToCourse(request.courseId(), request.facultyId());
        return ResponseEntity.ok("Faculty assigned to course");
    }


    /**
     * Get all courses - GET /api/faculty/courses
     */
    @GetMapping("/courses")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllCourses(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getRole().equals("FACULTY")) {
            return ResponseEntity.ok(courseService.findByFacultyId(principal.getId()));
        }
        return ResponseEntity.ok(courseService.findAll());
    }

    /**
     * Get all semesters - GET /api/faculty/semesters
     */
    @GetMapping("/semesters")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllSemesters() {
        return ResponseEntity.ok(semesterService.findAll());
    }

    /**
     * Get enrollments by course - GET /api/faculty/enrollments/course/{courseId}
     */
    @GetMapping("/enrollments/course/{courseId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<?> getEnrollmentsByCourse(@PathVariable Long courseId, @AuthenticationPrincipal UserPrincipal principal) {
        Course course = courseService.findById(courseId)
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
                    Course course = courseService.findById(e.courseId()).orElse(null);
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
                    Course course = courseService.findById(e.courseId()).orElse(null);
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
        Enrollment enrollment = enrollmentService.findEnrollmentById(request.enrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        if (principal.getRole().equals("FACULTY")) {
            Course course = enrollment.getCourse();
            if (course == null || course.getFaculty() == null || !course.getFaculty().getId().equals(principal.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You are not assigned to this course.");
            }
        }

        Enrollment saved = enrollmentService.updateGrade(request.enrollmentId(), request.grade(), principal.getUsername());
        
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
                Enrollment enrollment = enrollmentService.findEnrollmentById(request.enrollmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + request.enrollmentId()));
                Course course = enrollment.getCourse();
                if (course == null || course.getFaculty() == null || !course.getFaculty().getId().equals(principal.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You are not assigned to course for enrollment " + request.enrollmentId());
                }
            }
        }

        List<Map<String, Object>> results = requests.stream().map(request -> {
            Enrollment enrollment = enrollmentService.findEnrollmentById(request.enrollmentId())
                    .orElse(null);
            
            if (enrollment != null) {
                Enrollment saved = enrollmentService.updateGrade(request.enrollmentId(), request.grade(), principal.getUsername());
                
                Map<String, Object> result = new HashMap<>();
                result.put("id", saved.getId());
                result.put("grade", saved.getGrade());
                return result;
            }
            return null;
        }).toList();

        return ResponseEntity.ok(results);
    }
}