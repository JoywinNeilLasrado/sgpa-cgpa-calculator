package com.gradecalculator.service;
import com.gradecalculator.util.ValidationUtil;

import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.EnrollmentResponse;
import com.gradecalculator.dto.GradeUpdateRequest;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.model.Student;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing course enrollments, grading scales, and detailed student mark registers.
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Retrieves all course enrollments registered in the system.
     *
     * @return a list of enrollment response DTOs containing complete student, course, and grade details
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findAll() {
        return enrollmentRepository.findAll().stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    /**
     * Retrieves all course enrollments registered for a specific student.
     *
     * @param studentId the database identifier of the student
     * @return a list of enrollment response DTOs for the matching student
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudentId(Long studentId) {
        ValidationUtil.requireNonNull(studentId, "Student ID cannot be null");
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    /**
     * Retrieves course enrollments registered for a specific student in a specific semester.
     *
     * @param studentId  the database identifier of the student
     * @param semesterId the database identifier of the semester
     * @return a list of enrollment response DTOs for the matching student and semester
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudentIdAndSemesterId(Long studentId, Long semesterId) {
        ValidationUtil.requireNonNull(studentId, "Student ID cannot be null");
        ValidationUtil.requireNonNull(semesterId, "Semester ID cannot be null");
        return enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    /**
     * Retrieves all enrollments registered in a specific course.
     *
     * @param courseId the database identifier of the course
     * @return a list of enrollment response DTOs for the course
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByCourseId(Long courseId) {
        ValidationUtil.requireNonNull(courseId, "Course ID cannot be null");
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    /**
     * Retrieves all enrollments registered in a specific semester.
     *
     * @param semesterId the database identifier of the semester
     * @return a list of enrollment response DTOs for the semester
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findBySemesterId(Long semesterId) {
        ValidationUtil.requireNonNull(semesterId, "Semester ID cannot be null");
        return enrollmentRepository.findBySemesterId(semesterId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    /**
     * Enrolls a student in a specific course and initializes their internal grade record.
     *
     * @param request the enrollment request containing student ID, course ID, and any initial marks
     * @return the saved enrollment response details
     */
    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        ValidationUtil.requireNonNull(request, "Enrollment request cannot be null");
        ValidationUtil.requireNonNull(request.getStudentId(), "Student ID cannot be null");
        ValidationUtil.requireNonNull(request.getCourseId(), "Course ID cannot be null");

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new IllegalArgumentException("This student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setCieMarks(request.getCieMarks());
        enrollment.setCieTheoryMarks(request.getCieTheoryMarks());
        enrollment.setCieLabMarks(request.getCieLabMarks());
        enrollment.setSeeMarks(request.getSeeMarks());
        enrollment.setGraceMarks(request.getGraceMarks());
        enrollment.calculateGrade();
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Log auditing
        com.gradecalculator.security.AuditLogger.logChangeEvent(
            "Enrollment", 
            saved.getId() != null ? saved.getId().toString() : "NEW", 
            "CREATE", 
            "None", 
            getEnrollmentStateString(saved)
        );

        return toEnrollmentResponse(saved);
    }

    /**
     * Updates an existing enrollment record, modifying course/student linkages and recalculating grades.
     *
     * @param id      the database identifier of the enrollment to update
     * @param request the updated enrollment details
     * @return the updated enrollment response DTO
     */
    @Transactional
    public EnrollmentResponse update(Long id, EnrollmentRequest request) {
        ValidationUtil.requireNonNull(id, "Enrollment ID cannot be null");
        ValidationUtil.requireNonNull(request, "Enrollment request cannot be null");
        ValidationUtil.requireNonNull(request.getStudentId(), "Student ID cannot be null");
        ValidationUtil.requireNonNull(request.getCourseId(), "Course ID cannot be null");

        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        // Capture previous state
        String previousState = getEnrollmentStateString(enrollment);

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
        enrollment.setCieMarks(request.getCieMarks());
        enrollment.setCieTheoryMarks(request.getCieTheoryMarks());
        enrollment.setCieLabMarks(request.getCieLabMarks());
        enrollment.setSeeMarks(request.getSeeMarks());
        enrollment.setGraceMarks(request.getGraceMarks());
        enrollment.calculateGrade();
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Log auditing
        com.gradecalculator.security.AuditLogger.logChangeEvent(
            "Enrollment", 
            saved.getId().toString(), 
            "UPDATE", 
            previousState, 
            getEnrollmentStateString(saved)
        );

        return toEnrollmentResponse(saved);
    }

    /**
     * Deletes an enrollment record from the system and generates an audit log entry.
     *
     * @param id the database identifier of the enrollment to delete
     */
    @Transactional
    public void delete(Long id) {
        ValidationUtil.requireNonNull(id, "Enrollment ID cannot be null");
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        String previousState = getEnrollmentStateString(enrollment);

        enrollmentRepository.delete(enrollment);

        // Log auditing
        com.gradecalculator.security.AuditLogger.logChangeEvent(
            "Enrollment", 
            id.toString(), 
            "DELETE", 
            previousState, 
            "DELETED"
        );
    }

    /**
     * Converts a raw Enrollment database entity into a flat, read-only EnrollmentResponse DTO.
     *
     * @param enrollment the raw database entity
     * @return the mapped enrollment response DTO
     */
    public EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        Course course = enrollment.getCourse();
        Student student = enrollment.getStudent();
        return new EnrollmentResponse(
                enrollment.getId(),
                student.getId(),
                student.getName(),
                student.getStudentId(),
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getCredits(),
                course.getCourseType() != null ? course.getCourseType().name() : "THEORY",
                course.getSemester().getId(),
                course.getSemester().getSemesterNumber(),
                enrollment.getGrade() != null ? enrollment.getGrade().getGrade() : null,
                enrollment.getGrade() != null ? enrollment.getGrade().getGradePoints() : 0,
                enrollment.getCreditPoints(),
                enrollment.getCieMarks(),
                enrollment.getCieTheoryMarks(),
                enrollment.getCieLabMarks(),
                enrollment.getTest1Marks(),
                enrollment.getTest2Marks(),
                enrollment.getAssignmentMarks(),
                enrollment.getOaaMarks(),
                enrollment.getRegularLabMarks(),
                enrollment.getLabTestMarks(),
                enrollment.getLabRecordMarks(),
                enrollment.getSeeMarks(),
                enrollment.getGraceMarks(),
                enrollment.getTotalMarks()
        );
    }

    /**
     * Looks up an enrollment entity directly by its primary key.
     *
     * @param id the enrollment database identifier
     * @return an Optional containing the Enrollment if found
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Enrollment> findEnrollmentById(Long id) {
        ValidationUtil.requireNonNull(id, "Enrollment ID cannot be null");
        return enrollmentRepository.findById(id);
    }

    /**
     * Manually overrides/sets the letter grade of a student enrollment.
     *
     * @param enrollmentId the identifier of the enrollment
     * @param grade        the target letter grade
     * @param username     the modifier username for audit tracing
     * @return the saved enrollment entity
     */
    @Transactional
    public Enrollment updateGrade(Long enrollmentId, LetterGrade grade, String username) {
        ValidationUtil.requireNonNull(enrollmentId, "Enrollment ID cannot be null");
        ValidationUtil.requireNonNull(grade, "Grade cannot be null");
        ValidationUtil.requireNonBlank(username, "Modifier username is required");

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        String previousState = getEnrollmentStateString(enrollment);

        enrollment.setGrade(grade);
        enrollment.setLastModifiedBy(username);
        enrollment.setLastModifiedAt(java.time.LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Log auditing
        com.gradecalculator.security.AuditLogger.logChangeEvent(
            "Enrollment", 
            saved.getId().toString(), 
            "UPDATE_GRADE", 
            previousState, 
            getEnrollmentStateString(saved)
        );

        return saved;
    }

    /**
     * Detailed grade/marks registration updating and grade recalculation.
     *
     * @param request  the detailed mark request details
     * @param username the modifier username for audit tracking
     * @return the updated enrollment entity
     */
    @Transactional
    public Enrollment updateMarks(GradeUpdateRequest request, String username) {
        ValidationUtil.requireNonNull(request, "Grade update request cannot be null");
        ValidationUtil.requireNonNull(request.enrollmentId(), "Enrollment ID cannot be null");
        ValidationUtil.requireNonBlank(username, "Modifier username is required");

        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        String previousState = getEnrollmentStateString(enrollment);

        enrollment.setCieMarks(request.cieMarks());
        enrollment.setCieTheoryMarks(request.cieTheoryMarks());
        enrollment.setCieLabMarks(request.cieLabMarks());
        
        enrollment.setTest1Marks(request.test1Marks());
        enrollment.setTest2Marks(request.test2Marks());
        enrollment.setAssignmentMarks(request.assignmentMarks());
        enrollment.setOaaMarks(request.oaaMarks());
        
        enrollment.setRegularLabMarks(request.regularLabMarks());
        enrollment.setLabTestMarks(request.labTestMarks());
        enrollment.setLabRecordMarks(request.labRecordMarks());
        
        enrollment.setSeeMarks(request.seeMarks());
        enrollment.setGraceMarks(request.graceMarks());
        enrollment.calculateGrade();
        enrollment.setLastModifiedBy(username);
        enrollment.setLastModifiedAt(java.time.LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Log auditing
        com.gradecalculator.security.AuditLogger.logChangeEvent(
            "Enrollment", 
            saved.getId().toString(), 
            "UPDATE_MARKS", 
            previousState, 
            getEnrollmentStateString(saved)
        );

        return saved;
    }

    private String getEnrollmentStateString(Enrollment e) {
        if (e == null) {
            return "null";
        }
        return String.format("studentId=%d, courseId=%d, cieMarks=%s, seeMarks=%s, grade=%s, totalMarks=%s",
                e.getStudent() != null ? e.getStudent().getId() : null,
                e.getCourse() != null ? e.getCourse().getId() : null,
                e.getCieMarks(),
                e.getSeeMarks(),
                e.getGrade() != null ? e.getGrade().name() : "null",
                e.getTotalMarks());
    }
}

