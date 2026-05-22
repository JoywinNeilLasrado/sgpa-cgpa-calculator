package com.gradecalculator.service;

import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Service for charts and rankings
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final GradeCalculationService gradeCalculationService;

    public AnalyticsService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
            GradeCalculationService gradeCalculationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.gradeCalculationService = gradeCalculationService;
    }

    /**
     * Get SGPA trends for a student (for charts)
     */
    public List<Map<String, Object>> getSGPATrends(Long studentId) {
        List<Map<String, Object>> trends = new ArrayList<>();
        var allEnrollments = enrollmentRepository.findByStudentId(studentId);
        
        // Group by semester
        Map<Long, List<Enrollment>> bySemester = allEnrollments.stream()
            .collect(Collectors.groupingBy(e -> e.getCourse().getSemester().getId()));
        
        bySemester.forEach((semId, enrollments) -> {
            int credits = 0, points = 0;
            for (Enrollment e : enrollments) {
                if (e.getGrade() != null && e.getGrade() != LetterGrade.F) {
                    credits += e.getCourse().getCredits();
                    points += e.getCourse().getCredits() * e.getGrade().getGradePoints();
                }
            }
            
            Map<String, Object> trend = new HashMap<>();
            trend.put("semester", "Sem " + enrollments.get(0).getCourse().getSemester().getSemesterNumber());
            trend.put("sgpa", credits > 0 ? Math.round((double) points / credits * 100) / 100 : 0);
            trends.add(trend);
        });
        
        return trends;
    }

    /**
     * Get class rankings (sorted by CGPA descending)
     */
    public List<Map<String, Object>> getRankings(int limit) {
        List<Map<String, Object>> rankings = new ArrayList<>();
        
        for (var student : studentRepository.findAll()) {
            double cgpa = gradeCalculationService.calculateOverallCGPA(student.getId()).getCgpa();
            
            Map<String, Object> rank = new HashMap<>();
            rank.put("studentId", student.getId());
            rank.put("name", student.getName());
            rank.put("rollNumber", student.getStudentId());
            rank.put("cgpa", cgpa);
            rankings.add(rank);
        }
        
        // Sort by CGPA descending
        rankings.sort((a, b) -> Double.compare((Double) b.get("cgpa"), (Double) a.get("cgpa")));
        
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get toppers (top N students)
     */
    public List<Map<String, Object>> getToppers(int count) {
        return getRankings(count);
    }

    /**
     * Get course-wise analytics
     */
    public Map<String, Object> getCourseAnalytics(Long courseId) {
        Map<String, Object> analytics = new HashMap<>();
        
        var enrollments = enrollmentRepository.findByCourseId(courseId);
        
        // Calculate average
        int totalCredits = 0;
        int totalPoints = 0;
        int passed = 0;
        int failed = 0;
        Map<String, Integer> gradeDist = new HashMap<>();

        for (var e : enrollments) {
            LetterGrade grade = e.getGrade();
            if (grade != null) {
                totalCredits += e.getCourse().getCredits();
                totalPoints += e.getCourse().getCredits() * grade.getGradePoints();
                
                if (grade != LetterGrade.F) {
                    passed++;
                } else {
                    failed++;
                }
                gradeDist.merge(grade.getGrade(), 1, Integer::sum);
            }
        }

        analytics.put("totalStudents", enrollments.size());
        analytics.put("passed", passed);
        analytics.put("failed", failed);
        analytics.put("averageGrade", totalCredits > 0 ? 
            Math.round((double) totalPoints / totalCredits * 100) / 100 : 0);
        analytics.put("gradeDistribution", gradeDist);

        return analytics;
    }

    /**
     * Get class statistics
     */
    public Map<String, Object> getClassStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        var rankings = getRankings(100);
        
        if (!rankings.isEmpty()) {
            double avgCGPA = rankings.stream()
                .mapToDouble(m -> (Double) m.get("cgpa"))
                .average()
                .orElse(0);
            
            long passCount = rankings.stream()
                .filter(m -> (Double) m.get("cgpa") >= 5.0)
                .count();
            
            stats.put("totalStudents", rankings.size());
            stats.put("averageCGPA", Math.round(avgCGPA * 100) / 100);
            stats.put("passRate", Math.round((double) passCount / rankings.size() * 100));
            stats.put("topper", rankings.get(0));
        }

        return stats;
    }
}