package com.gradecalculator.controller;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.*;
import com.gradecalculator.service.GradeCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
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

    @GetMapping("/students/{id}/dashboard")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        CgpaResponse cgpa = gradeCalculationService.calculateOverallCGPA(id);
        List<Map<String, Object>> semesters = new ArrayList<>();

        for (Semester semester : semesterRepository.findAll()) {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndSemesterId(id, semester.getId());
            if (enrollments.isEmpty()) {
                continue;
            }

            int totalCredits = enrollments.stream()
                    .map(Enrollment::getCourse)
                    .filter(course -> course != null && course.getCredits() != null)
                    .mapToInt(Course::getCredits)
                    .sum();

            SgpaResponse sgpa = gradeCalculationService.calculateSGPA(id, semester.getId());
            semesters.add(Map.of(
                    "semesterId", semester.getId(),
                    "semesterNumber", semester.getSemesterNumber(),
                    "sgpa", sgpa.getSgpa(),
                    "totalCredits", totalCredits
            ));
        }

        return ResponseEntity.ok(Map.of(
                "student", student,
                "overallCgpa", cgpa.getCgpa(),
                "totalCredits", cgpa.getTotalEarnedCredits(),
                "semesters", semesters
        ));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@RequestBody Map<String, String> request) {
        validateText(request.get("name"), "Student name is required");
        validateText(request.get("studentId"), "Roll number is required");
        studentRepository.findByStudentId(request.get("studentId"))
                .ifPresent(student -> {
                    throw new IllegalArgumentException("A student with this roll number already exists");
                });

        Student student = new Student(request.get("name"), request.get("studentId"));
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        validateText(request.get("name"), "Student name is required");
        validateText(request.get("studentId"), "Roll number is required");

        studentRepository.findByStudentId(request.get("studentId"))
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A student with this roll number already exists");
                });

        student.setName(request.get("name"));
        student.setStudentId(request.get("studentId"));
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @DeleteMapping("/students/{id}")
    @Transactional
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudentId(id));
        studentRepository.delete(student);
        return ResponseEntity.noContent().build();
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
        Integer semesterNumber = request.get("semesterNumber");
        if (semesterNumber == null || semesterNumber < 1) {
            throw new IllegalArgumentException("Semester number must be greater than zero");
        }

        Semester semester = new Semester(request.get("semesterNumber"));
        return ResponseEntity.ok(semesterRepository.save(semester));
    }

    @PutMapping("/semesters/{id}")
    public ResponseEntity<Semester> updateSemester(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        Integer semesterNumber = request.get("semesterNumber");
        if (semesterNumber == null || semesterNumber < 1) {
            throw new IllegalArgumentException("Semester number must be greater than zero");
        }

        semester.setSemesterNumber(semesterNumber);
        return ResponseEntity.ok(semesterRepository.save(semester));
    }

    @DeleteMapping("/semesters/{id}")
    @Transactional
    public ResponseEntity<Void> deleteSemester(@PathVariable Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        List<Course> courses = courseRepository.findBySemesterId(id);
        for (Course course : courses) {
            enrollmentRepository.deleteAll(enrollmentRepository.findByCourseId(course.getId()));
        }
        courseRepository.deleteAll(courses);
        semesterRepository.delete(semester);
        return ResponseEntity.noContent().build();
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
        validateText(valueAsString(request.get("courseCode")), "Course code is required");
        validateText(valueAsString(request.get("courseName")), "Course name is required");
        Integer credits = parsePositiveInt(request.get("credits"), "Credits must be greater than zero");
        courseRepository.findByCourseCodeAndSemesterId(valueAsString(request.get("courseCode")), semesterId)
                .ifPresent(course -> {
                    throw new IllegalArgumentException("A course with this code already exists in the semester");
                });

        Course course = new Course(
                request.get("courseCode").toString(),
                request.get("courseName").toString(),
                credits
        );
        course.setSemester(semester);
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Long semesterId = Long.parseLong(request.get("semesterId").toString());
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        validateText(valueAsString(request.get("courseCode")), "Course code is required");
        validateText(valueAsString(request.get("courseName")), "Course name is required");

        courseRepository.findByCourseCodeAndSemesterId(valueAsString(request.get("courseCode")), semesterId)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A course with this code already exists in the semester");
                });

        course.setCourseCode(valueAsString(request.get("courseCode")));
        course.setCourseName(valueAsString(request.get("courseName")));
        course.setCredits(parsePositiveInt(request.get("credits"), "Credits must be greater than zero"));
        course.setSemester(semester);
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @DeleteMapping("/courses/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        enrollmentRepository.deleteAll(enrollmentRepository.findByCourseId(id));
        courseRepository.delete(course);
        return ResponseEntity.noContent().build();
    }

    // ============ Enrollment Endpoints ============

    @GetMapping("/enrollments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentRepository.findAll().stream()
                .map(this::toEnrollmentResponse)
                .toList());
    }

    @GetMapping("/enrollments/student/{studentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toEnrollmentResponse)
                .toList());
    }

    @GetMapping("/enrollments/student/{studentId}/semester/{semesterId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getEnrollmentsByStudentAndSemester(
            @PathVariable Long studentId, 
            @PathVariable Long semesterId) {
        return ResponseEntity.ok(enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId).stream()
                .map(this::toEnrollmentResponse)
                .toList());
    }

    @GetMapping("/results/student/{studentId}/semester/{semesterId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getSemesterResult(
            @PathVariable Long studentId,
            @PathVariable Long semesterId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

        return ResponseEntity.ok(enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId).stream()
                .sorted(Comparator.comparing(enrollment -> enrollment.getCourse().getCourseCode()))
                .map(enrollment -> {
                    Course course = enrollment.getCourse();
                    LetterGrade grade = enrollment.getGrade();
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("courseCode", course.getCourseCode());
                    row.put("courseName", course.getCourseName());
                    row.put("credits", course.getCredits());
                    row.put("grade", grade.getGrade());
                    row.put("gradePoints", grade.getGradePoints());
                    row.put("creditPoints", enrollment.getCreditPoints());
                    return row;
                })
                .toList());
    }

    @PostMapping("/enrollments")
    public ResponseEntity<Map<String, Object>> createEnrollment(@RequestBody EnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new IllegalArgumentException("This student is already enrolled in this course");
        }

        LetterGrade grade = LetterGrade.fromGrade(request.getGrade());
        
        Enrollment enrollment = new Enrollment(student, course, grade);
        return ResponseEntity.ok(toEnrollmentResponse(enrollmentRepository.save(enrollment)));
    }

    @PutMapping("/enrollments/{id}")
    public ResponseEntity<Map<String, Object>> updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        enrollmentRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(id))
                .filter(existing -> existing.getStudent().getId().equals(student.getId()))
                .filter(existing -> existing.getCourse().getId().equals(course.getId()))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("This student is already enrolled in this course");
                });

        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setGrade(LetterGrade.fromGrade(request.getGrade()));
        return ResponseEntity.ok(toEnrollmentResponse(enrollmentRepository.save(enrollment)));
    }

    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        enrollmentRepository.delete(enrollment);
        return ResponseEntity.noContent().build();
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
            throw new IllegalArgumentException(e.getMessage());
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
            throw new IllegalArgumentException(e.getMessage());
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
            throw new IllegalArgumentException(e.getMessage());
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    private Map<String, Object> toEnrollmentResponse(Enrollment enrollment) {
        Map<String, Object> response = new LinkedHashMap<>();
        Course course = enrollment.getCourse();
        Student student = enrollment.getStudent();
        response.put("id", enrollment.getId());
        response.put("studentId", student.getId());
        response.put("studentName", student.getName());
        response.put("studentRollNumber", student.getStudentId());
        response.put("courseId", course.getId());
        response.put("courseCode", course.getCourseCode());
        response.put("courseName", course.getCourseName());
        response.put("credits", course.getCredits());
        response.put("semesterId", course.getSemester().getId());
        response.put("semesterNumber", course.getSemester().getSemesterNumber());
        response.put("grade", enrollment.getGrade().getGrade());
        response.put("gradePoints", enrollment.getGrade().getGradePoints());
        response.put("creditPoints", enrollment.getCreditPoints());
        return response;
    }

    private void validateText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer parsePositiveInt(Object value, String message) {
        try {
            int parsed = Integer.parseInt(value.toString());
            if (parsed < 1) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(message);
        }
    }
}
