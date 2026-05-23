package com.gradecalculator.service;

import com.gradecalculator.dto.*;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

        // Get all enrollments for this student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        // Calculate CGPA
        CgpaResponse cgpa = gradeCalculationService.calculateOverallCGPA(studentId);
        
        // Build response
        DashboardResponse response = new DashboardResponse();
        response.setStudentId(student.getId());
        response.setStudentName(student.getName());
        response.setStudentRoll(student.getStudentId());
        response.setCgpa(cgpa.getCgpa());
        response.setTotalCredits(cgpa.getTotalEarnedCredits());
        response.setTotalCourses((int) enrollments.stream().filter(e -> e.getGrade() != LetterGrade.F).count());
        response.setSemestersCompleted(cgpa.getSemestersCompleted());

        // Calculate SGPA by semester
        Map<String, Double> sgpaBySemester = new LinkedHashMap<>();
        Map<String, Integer> gradeDistribution = new HashMap<>();
        List<SemesterResultResponse> semesterResults = new ArrayList<>();

        // Get all semesters and sort by number
        List<Semester> semesters = semesterRepository.findAll();
        semesters.sort(Comparator.comparing(Semester::getSemesterNumber));

        int totalPassed = 0;
        int totalWithGrades = 0;
        int outstandingCount = 0;

        for (Semester sem : semesters) {
            List<Enrollment> semEnrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, sem.getId());
            if (semEnrollments.isEmpty()) continue;

            // Calculate SGPA for this semester
            int semCredits = 0;
            int semPoints = 0;
            List<CourseResultResponse> courseResults = new ArrayList<>();

            for (Enrollment e : semEnrollments) {
                Course course = e.getCourse();
                LetterGrade grade = e.getGrade();
                
                if (grade != null) {
                    totalWithGrades++;
                    int gp = grade.getGradePoints();
                    int cp = course.getCredits() * gp;
                    semCredits += course.getCredits();
                    semPoints += cp;
                    
                    if (grade != LetterGrade.F) {
                        totalPassed++;
                    }
                    
                    // Count grades
                    gradeDistribution.put(grade.getGrade(), gradeDistribution.getOrDefault(grade.getGrade(), 0) + 1);
                    
                    // Count outstanding
                    if (grade == LetterGrade.O) outstandingCount++;
                    
                    // Add course result
                    courseResults.add(new CourseResultResponse(
                            course.getCourseCode(),
                            course.getCourseName(),
                            course.getCredits(),
                            grade.getGrade(),
                            gp,
                            cp
                    ));
                } else {
                    // Add ungraded course result
                    courseResults.add(new CourseResultResponse(
                            course.getCourseCode(),
                            course.getCourseName(),
                            course.getCredits(),
                            null,
                            0,
                            0
                    ));
                }
            }

            double semGpa = semCredits > 0 ? roundToTwo((double) semPoints / semCredits) : 0.0;
            if (semCredits > 0) {
                sgpaBySemester.put("Sem " + sem.getSemesterNumber(), semGpa);
            }
            
            // Create semester result for transcript
            SemesterResultResponse semResult = new SemesterResultResponse(sem.getSemesterNumber(), semGpa, semCredits, semPoints);
            semResult.setCourses(courseResults);
            semesterResults.add(semResult);
        }

        response.setSgpaBySemester(sgpaBySemester);
        response.setGradeDistribution(gradeDistribution);
        response.setPassRate(totalWithGrades > 0 ? (totalPassed * 100 / totalWithGrades) : 0);
        response.setOutstandingCount(outstandingCount);
        response.setSemesterResults(semesterResults);

        return response;
    }

    @Transactional(readOnly = true)
    public List<SemesterResultRowResponse> getSemesterResult(Long studentId, Long semesterId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId);
        
        return enrollments.stream()
                .sorted(Comparator.comparing(e -> e.getCourse().getCourseCode()))
                .map(e -> {
                    Course course = e.getCourse();
                    LetterGrade grade = e.getGrade();
                    return new SemesterResultRowResponse(
                            course.getCourseCode(),
                            course.getCourseName(),
                            course.getCredits(),
                            grade != null ? grade.getGrade() : null,
                            grade != null ? grade.getGradePoints() : 0,
                            grade != null ? course.getCredits() * grade.getGradePoints() : 0
                    );
                })
                .collect(Collectors.toList());
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}