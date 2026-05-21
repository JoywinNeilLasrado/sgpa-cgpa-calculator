package com.gradecalculator.service;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.DashboardResponse;
import com.gradecalculator.dto.SemesterResultRowResponse;
import com.gradecalculator.dto.SemesterSummaryResponse;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.dto.StudentSummaryResponse;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.model.Semester;
import com.gradecalculator.model.Student;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeCalculationService gradeCalculationService;

    public DashboardService(
            StudentRepository studentRepository,
            SemesterRepository semesterRepository,
            EnrollmentRepository enrollmentRepository,
            GradeCalculationService gradeCalculationService) {
        this.studentRepository = studentRepository;
        this.semesterRepository = semesterRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeCalculationService = gradeCalculationService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getStudentDashboard(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        CgpaResponse cgpa = gradeCalculationService.calculateOverallCGPA(studentId);
        List<SemesterSummaryResponse> semesters = semesterRepository.findAll().stream()
                .map(semester -> toSemesterSummary(studentId, semester))
                .filter(summary -> summary != null)
                .toList();

        return new DashboardResponse(
                new StudentSummaryResponse(student.getId(), student.getName(), student.getStudentId()),
                cgpa.getCgpa(),
                cgpa.getTotalEarnedCredits(),
                semesters
        );
    }

    @Transactional(readOnly = true)
    public List<SemesterResultRowResponse> getSemesterResult(Long studentId, Long semesterId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

        return enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId).stream()
                .sorted(Comparator.comparing(enrollment -> enrollment.getCourse().getCourseCode()))
                .map(this::toSemesterResultRow)
                .toList();
    }

    private SemesterSummaryResponse toSemesterSummary(Long studentId, Semester semester) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, semester.getId());
        if (enrollments.isEmpty()) {
            return null;
        }

        int totalCredits = enrollments.stream()
                .map(Enrollment::getCourse)
                .filter(course -> course != null && course.getCredits() != null)
                .mapToInt(Course::getCredits)
                .sum();

        SgpaResponse sgpa = gradeCalculationService.calculateSGPA(studentId, semester.getId());
        return new SemesterSummaryResponse(semester.getId(), semester.getSemesterNumber(), sgpa.getSgpa(), totalCredits);
    }

    private SemesterResultRowResponse toSemesterResultRow(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        LetterGrade grade = enrollment.getGrade();
        return new SemesterResultRowResponse(
                course.getCourseCode(),
                course.getCourseName(),
                course.getCredits(),
                grade.getGrade(),
                grade.getGradePoints(),
                enrollment.getCreditPoints()
        );
    }
}
