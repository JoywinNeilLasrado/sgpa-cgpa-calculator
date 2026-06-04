package com.gradecalculator.service;

import com.gradecalculator.dto.response.CgpaResponse;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private GradeCalculationService gradeCalculationService;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(enrollmentRepository, studentRepository, gradeCalculationService);
    }

    @Test
    void getSGPATrendsCalculatesTrendsCorrectly() {
        Long studentId = 1L;
        
        Semester sem1 = new Semester(1);
        sem1.setId(10L);
        Semester sem2 = new Semester(2);
        sem2.setId(20L);

        Course c1 = new Course("CS101", "Intro to CS", 4);
        c1.setSemester(sem1);
        Course c2 = new Course("CS102", "Data Structures", 4);
        c2.setSemester(sem1);
        Course c3 = new Course("CS201", "Algorithms", 4);
        c3.setSemester(sem2);

        Enrollment e1 = new Enrollment(new Student(), c1, LetterGrade.O); // GP 10, CP 40
        Enrollment e2 = new Enrollment(new Student(), c2, LetterGrade.F); // GP 0 (F grade: excluded from credits/points in trends calculation logic)
        Enrollment e3 = new Enrollment(new Student(), c3, LetterGrade.A); // GP 8, CP 32

        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(Arrays.asList(e1, e2, e3));

        List<Map<String, Object>> result = service.getSGPATrends(studentId);

        assertThat(result).hasSize(2);
        
        // Find Sem 1 trend
        Map<String, Object> sem1Trend = result.stream()
                .filter(t -> t.get("semester").equals("Sem 1"))
                .findFirst().orElseThrow();
        // CS101 is O (10 pts, 4 credits). CS102 is F (excluded). Sem 1 SGPA = 40 / 4 = 10.0
        assertThat(sem1Trend.get("sgpa")).isEqualTo(10.0);

        // Find Sem 2 trend
        Map<String, Object> sem2Trend = result.stream()
                .filter(t -> t.get("semester").equals("Sem 2"))
                .findFirst().orElseThrow();
        // CS201 is A (8 pts, 4 credits). Sem 2 SGPA = 32 / 4 = 8.0
        assertThat(sem2Trend.get("sgpa")).isEqualTo(8.0);
    }

    @Test
    void getRankingsSortsStudentsByCgpaDescending() {
        Student s1 = new Student("Alice", "CS2024001");
        s1.setId(1L);
        s1.setBranch("IT");
        Student s2 = new Student("Bob", "CS2024002");
        s2.setId(2L);
        s2.setBranch("CS");

        when(studentRepository.findAll()).thenReturn(Arrays.asList(s1, s2));
        
        when(gradeCalculationService.calculateOverallCGPA(1L))
                .thenReturn(new CgpaResponse(1L, 8.5, 20, 170, 4));
        when(gradeCalculationService.calculateOverallCGPA(2L))
                .thenReturn(new CgpaResponse(2L, 9.2, 20, 184, 4));

        List<Map<String, Object>> result = service.getRankings(5);

        assertThat(result).hasSize(2);
        // Bob has 9.2, Alice has 8.5. So Bob should be first.
        assertThat(result.get(0).get("name")).isEqualTo("Bob");
        assertThat(result.get(0).get("cgpa")).isEqualTo(9.2);
        assertThat(result.get(0).get("branch")).isEqualTo("CS");

        assertThat(result.get(1).get("name")).isEqualTo("Alice");
        assertThat(result.get(1).get("cgpa")).isEqualTo(8.5);
        assertThat(result.get(1).get("branch")).isEqualTo("IT");
    }

    @Test
    void getToppersReturnsLimitedRankings() {
        Student s1 = new Student("Alice", "CS2024001");
        s1.setId(1L);
        Student s2 = new Student("Bob", "CS2024002");
        s2.setId(2L);

        when(studentRepository.findAll()).thenReturn(Arrays.asList(s1, s2));
        when(gradeCalculationService.calculateOverallCGPA(1L)).thenReturn(new CgpaResponse(1L, 8.5, 20, 170, 4));
        when(gradeCalculationService.calculateOverallCGPA(2L)).thenReturn(new CgpaResponse(2L, 9.2, 20, 184, 4));

        List<Map<String, Object>> result = service.getToppers(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("name")).isEqualTo("Bob");
    }

    @Test
    void getCourseAnalyticsCalculatesCorrectly() {
        Long courseId = 100L;
        Course course = new Course("CS101", "Intro to CS", 4);
        
        Enrollment e1 = new Enrollment(new Student(), course, LetterGrade.O);
        Enrollment e2 = new Enrollment(new Student(), course, LetterGrade.A);
        Enrollment e3 = new Enrollment(new Student(), course, LetterGrade.F);

        when(enrollmentRepository.findByCourseId(courseId)).thenReturn(Arrays.asList(e1, e2, e3));

        Map<String, Object> result = service.getCourseAnalytics(courseId);

        assertThat(result.get("totalStudents")).isEqualTo(3);
        assertThat(result.get("passed")).isEqualTo(2);
        assertThat(result.get("failed")).isEqualTo(1);
        
        // Total credits = 12, Total points = (4*10) + (4*8) + (4*0) = 72
        // Average grade points = 72 / 12 = 6.0
        assertThat(result.get("averageGrade")).isEqualTo(6.0);

        Map<?, ?> dist = (Map<?, ?>) result.get("gradeDistribution");
        assertThat(dist.get("O")).isEqualTo(1);
        assertThat(dist.get("A")).isEqualTo(1);
        assertThat(dist.get("F")).isEqualTo(1);
    }

    @Test
    void getClassStatisticsCalculatesAggregatesCorrectly() {
        Student s1 = new Student("Alice", "CS2024001");
        s1.setId(1L);
        Student s2 = new Student("Bob", "CS2024002");
        s2.setId(2L);
        Student s3 = new Student("Charlie", "CS2024003");
        s3.setId(3L);

        when(studentRepository.findAll()).thenReturn(Arrays.asList(s1, s2, s3));
        when(gradeCalculationService.calculateOverallCGPA(1L)).thenReturn(new CgpaResponse(1L, 8.5, 20, 170, 4));
        when(gradeCalculationService.calculateOverallCGPA(2L)).thenReturn(new CgpaResponse(2L, 9.2, 20, 184, 4));
        // Charlie has a CGPA below 5.0 (fail rate check)
        when(gradeCalculationService.calculateOverallCGPA(3L)).thenReturn(new CgpaResponse(3L, 4.5, 20, 90, 4));

        Map<String, Object> result = service.getClassStatistics();

        assertThat(result.get("totalStudents")).isEqualTo(3);
        // Average CGPA = (9.2 + 8.5 + 4.5) / 3 = 7.4
        assertThat(result.get("averageCGPA")).isEqualTo(7.4);
        // Pass count: 2 (Bob & Alice), Charlie failed (< 5.0)
        // Pass rate = 2 / 3 = 67%
        assertThat(result.get("passRate")).isEqualTo(67L);
        
        Map<?, ?> topper = (Map<?, ?>) result.get("topper");
        assertThat(topper.get("name")).isEqualTo("Bob");
    }
}
