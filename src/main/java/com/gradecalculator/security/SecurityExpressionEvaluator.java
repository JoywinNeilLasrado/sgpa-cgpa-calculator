package com.gradecalculator.security;

import com.gradecalculator.model.Student;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.repository.StudentRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Custom security expression evaluator for granular object-level access controls.
 */
@Component("sec")
public class SecurityExpressionEvaluator {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public SecurityExpressionEvaluator(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Check if the authenticated user has ADMIN or FACULTY privileges,
     * or if they are the student owner of the resource.
     */
    public boolean isStudentOwner(UserPrincipal principal, Long studentId) {
        if (principal == null || studentId == null) {
            return false;
        }
        String role = principal.getRole();
        if ("ADMIN".equals(role) || "FACULTY".equals(role)) {
            return true;
        }
        if ("STUDENT".equals(role)) {
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            return studentOpt.map(student -> student.getUsername() != null && student.getUsername().equals(principal.getUsername())).orElse(false);
        }
        return false;
    }

    /**
     * Check if the authenticated user matches the target username (self-service).
     */
    public boolean isSelf(UserPrincipal principal, String username) {
        if (principal == null || username == null) {
            return false;
        }
        return principal.getUsername().equalsIgnoreCase(username);
    }

    /**
     * Check if the authenticated user is authorized to modify a specific enrollment.
     * Allowed if the user is an ADMIN, or if they are a FACULTY member assigned to the course.
     */
    public boolean isAuthorizedToModifyEnrollment(UserPrincipal principal, Long enrollmentId) {
        if (principal == null || enrollmentId == null) {
            return false;
        }
        String role = principal.getRole();
        if ("ADMIN".equals(role)) {
            return true;
        }
        if ("FACULTY".equals(role)) {
            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
            return enrollmentOpt.map(enrollment -> 
                enrollment.getCourse() != null && 
                enrollment.getCourse().getFaculty() != null && 
                enrollment.getCourse().getFaculty().getUsername() != null && 
                enrollment.getCourse().getFaculty().getUsername().equals(principal.getUsername())
            ).orElse(false);
        }
        return false;
    }
}
