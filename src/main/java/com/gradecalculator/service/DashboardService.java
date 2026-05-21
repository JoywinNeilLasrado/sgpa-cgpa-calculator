package com.gradecalculator.service;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.SgpaResponse;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Object> getStudentDashboard(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        CgpaResponse cgpa = gradeCalculationService.calculateOverallCGPA(studentId);
        List<Map<String, Object>> semesters = new ArrayList<>();

        for (Semester semester : semesterRepository.findAll()) {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, semester.getId());
            if (enrollments.isEmpty()) {
                continue;
            }

            int totalCredits = enrollments.stream()
                    .map(Enrollment::getCourse)
                    .filter(course -> course != null && course.getCredits() != null)
                    .mapToInt(Course::getCredits)
                    .sum();

            SgpaResponse sgpa = gradeCalculationService.calculateSGPA(studentId, semester.getId());
            Map<String, Object> semesterSummary = new LinkedHashMap<>();
            semesterSummary.put("semesterId", semester.getId());
            semesterSummary.put("semesterNumber", semester.getSemesterNumber());
            semesterSummary.put("sgpa", sgpa.getSgpa());
            semesterSummary.put("totalCredits", totalCredits);
            semesters.add(semesterSummary);
        }

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("student", student);
        dashboard.put("overallCgpa", cgpa.getCgpa());
        dashboard.put("totalCredits", cgpa.getTotalEarnedCredits());
        dashboard.put("semesters", semesters);
        return dashboard;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSemesterResult(Long studentId, Long semesterId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

        return enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId).stream()
                .sorted(Comparator.comparing(enrollment -> enrollment.getCourse().getCourseCode()))
                .map(this::toSemesterResultRow)
                .toList();
    }

    private Map<String, Object> toSemesterResultRow(Enrollment enrollment) {
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
    }
}
