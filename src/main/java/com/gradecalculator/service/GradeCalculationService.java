package com.gradecalculator.service;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

/**
 * Service for calculating SGPA and CGPA.
 * Implements the formulas according to the autonomous regulations.
 */
@Service
@Transactional
public class GradeCalculationService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public GradeCalculationService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    private static class GradeSummary {
        final int totalCreditPoints;
        final int totalCredits;

        GradeSummary(int totalCreditPoints, int totalCredits) {
            this.totalCreditPoints = totalCreditPoints;
            this.totalCredits = totalCredits;
        }
    }

    private GradeSummary summarizeGrades(List<Enrollment> enrollments, java.util.function.Predicate<Enrollment> filter) {
        int totalCreditPoints = 0;
        int totalCredits = 0;

        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            LetterGrade grade = enrollment.getGrade();

            if (course != null && course.getCredits() != null && grade != null && filter.test(enrollment)) {
                int creditPoints = course.getCredits() * grade.getGradePoints();
                totalCreditPoints += creditPoints;
                totalCredits += course.getCredits();
            }
        }
        return new GradeSummary(totalCreditPoints, totalCredits);
    }

    /**
     * Calculate SGPA for a student in a specific semester.
     * 
     * SGPA = Sum(Credit Points) / Sum(Course Credits)
     * where Credit Points = Course Credits × Grade Points
     */
    public SgpaResponse calculateSGPA(Long studentId, Long semesterId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId);

        if (enrollments.isEmpty()) {
            throw new IllegalArgumentException("No enrollments found for this student in the specified semester");
        }

        GradeSummary summary = summarizeGrades(enrollments, e -> true);

        if (summary.totalCredits == 0) {
            return new SgpaResponse(studentId, semesterId, 0.0, 0, 0);
        }

        double sgpa = roundToTwoDecimals((double) summary.totalCreditPoints / summary.totalCredits);

        return new SgpaResponse(studentId, semesterId, sgpa, summary.totalCredits, summary.totalCreditPoints);
    }

    /**
     * Calculate CGPA for a student from first semester up to the specified semester.
     * 
     * CGPA = Sum(Credit Points for all courses excluding F grades) / 
     *        Sum(Course Credits excluding F grades)
     */
    public CgpaResponse calculateCGPA(Long studentId, Long semesterId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Get all enrollments, excluding F grades
        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentId(studentId);

        Set<Long> countedSemesters = new java.util.HashSet<>();
        
        GradeSummary summary = summarizeGrades(allEnrollments, e -> {
            Course course = e.getCourse();
            LetterGrade grade = e.getGrade();

            if (course == null || course.getSemester() == null) {
                return false;
            }

            Long enrollSemesterId = course.getSemester().getId();
            
            // Only count enrollments up to the specified semester
            if (enrollSemesterId > semesterId) {
                return false;
            }

            // Exclude F grades from both numerator and denominator
            if (grade == LetterGrade.F) {
                return false;
            }

            countedSemesters.add(enrollSemesterId);
            return true;
        });

        int semestersCompleted = countedSemesters.size();

        if (summary.totalCredits == 0) {
            return new CgpaResponse(studentId, 0.0, 0, 0, semestersCompleted);
        }

        double cgpa = roundToTwoDecimals((double) summary.totalCreditPoints / summary.totalCredits);

        return new CgpaResponse(studentId, cgpa, summary.totalCredits, summary.totalCreditPoints, semestersCompleted);
    }

    /**
     * Calculate CGPA for a student across all enrolled semesters.
     */
    public CgpaResponse calculateOverallCGPA(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentId(studentId);

        Set<Long> semestersCompleted = new java.util.HashSet<>();

        GradeSummary summary = summarizeGrades(allEnrollments, e -> {
            Course course = e.getCourse();
            LetterGrade grade = e.getGrade();

            if (course == null) {
                return false;
            }

            // Exclude F grades
            if (grade == LetterGrade.F) {
                return false;
            }

            if (course.getSemester() != null) {
                semestersCompleted.add(course.getSemester().getId());
            }
            return true;
        });

        if (summary.totalCredits == 0) {
            return new CgpaResponse(studentId, 0.0, 0, 0, semestersCompleted.size());
        }

        double cgpa = roundToTwoDecimals((double) summary.totalCreditPoints / summary.totalCredits);

        return new CgpaResponse(studentId, cgpa, summary.totalCredits, summary.totalCreditPoints, semestersCompleted.size());
    }

    /**
     * Round a double value to two decimal places.
     */
    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
