package com.gradecalculator.service;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.model.Student;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    /**
     * Calculate SGPA for a student in a specific semester.
     * 
     * SGPA = Sum(Credit Points) / Sum(Course Credits)
     * where Credit Points = Course Credits × Grade Points
     */
    public SgpaResponse calculateSGPA(Long studentId, Long semesterId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId);

        if (enrollments.isEmpty()) {
            throw new IllegalArgumentException("No enrollments found for this student in the specified semester");
        }

        int totalCreditPoints = 0;
        int totalCredits = 0;

        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            LetterGrade grade = enrollment.getGrade();

            if (course != null && course.getCredits() != null && grade != null) {
                int creditPoints = course.getCredits() * grade.getGradePoints();
                totalCreditPoints += creditPoints;
                totalCredits += course.getCredits();
            }
        }

        if (totalCredits == 0) {
            throw new IllegalStateException("No valid courses found for SGPA calculation");
        }

        double sgpa = roundToTwoDecimals((double) totalCreditPoints / totalCredits);

        return new SgpaResponse(studentId, semesterId, sgpa, totalCredits, totalCreditPoints);
    }

    /**
     * Calculate CGPA for a student from first semester up to the specified semester.
     * 
     * CGPA = Sum(Credit Points for all courses excluding F grades) / 
     *        Sum(Course Credits excluding F grades)
     */
    public CgpaResponse calculateCGPA(Long studentId, Long semesterId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Get all enrollments up to the specified semester, excluding F grades
        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentId(studentId);

        int totalValidCreditPoints = 0;
        int totalValidCredits = 0;
        int semestersCompleted = 0;
        Set<Long> countedSemesters = new java.util.HashSet<>();

        for (Enrollment enrollment : allEnrollments) {
            Course course = enrollment.getCourse();
            LetterGrade grade = enrollment.getGrade();

            if (course == null || course.getSemester() == null) {
                continue;
            }

            Long enrollSemesterId = course.getSemester().getId();
            
            // Only count enrollments up to the specified semester
            if (enrollSemesterId > semesterId) {
                continue;
            }

            // Exclude F grades from both numerator and denominator
            if (grade == LetterGrade.F) {
                continue;
            }

            if (course.getCredits() != null && grade != null) {
                int creditPoints = course.getCredits() * grade.getGradePoints();
                totalValidCreditPoints += creditPoints;
                totalValidCredits += course.getCredits();

                if (!countedSemesters.contains(enrollSemesterId)) {
                    countedSemesters.add(enrollSemesterId);
                }
            }
        }

        semestersCompleted = countedSemesters.size();

        if (totalValidCredits == 0) {
            throw new IllegalStateException("No valid courses found for CGPA calculation (all grades may be F)");
        }

        double cgpa = roundToTwoDecimals((double) totalValidCreditPoints / totalValidCredits);

        return new CgpaResponse(studentId, cgpa, totalValidCredits, totalValidCreditPoints, semestersCompleted);
    }

    /**
     * Calculate CGPA for a student across all enrolled semesters.
     */
    public CgpaResponse calculateOverallCGPA(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentId(studentId);

        int totalValidCreditPoints = 0;
        int totalValidCredits = 0;
        Set<Long> semestersCompleted = new java.util.HashSet<>();

        for (Enrollment enrollment : allEnrollments) {
            Course course = enrollment.getCourse();
            LetterGrade grade = enrollment.getGrade();

            if (course == null) {
                continue;
            }

            // Exclude F grades
            if (grade == LetterGrade.F) {
                continue;
            }

            if (course.getCredits() != null && grade != null) {
                int creditPoints = course.getCredits() * grade.getGradePoints();
                totalValidCreditPoints += creditPoints;
                totalValidCredits += course.getCredits();

                if (course.getSemester() != null) {
                    semestersCompleted.add(course.getSemester().getId());
                }
            }
        }

        if (totalValidCredits == 0) {
            throw new IllegalStateException("No valid courses found for CGPA calculation");
        }

        double cgpa = roundToTwoDecimals((double) totalValidCreditPoints / totalValidCredits);

        return new CgpaResponse(studentId, cgpa, totalValidCredits, totalValidCreditPoints, semestersCompleted.size());
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
