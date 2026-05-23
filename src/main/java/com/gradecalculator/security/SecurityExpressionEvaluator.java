package com.gradecalculator.security;

import com.gradecalculator.model.Student;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Custom security expression evaluator for granular object-level access controls.
 */
@Component("sec")
public class SecurityExpressionEvaluator {

    private final StudentRepository studentRepository;

    public SecurityExpressionEvaluator(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
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
}
