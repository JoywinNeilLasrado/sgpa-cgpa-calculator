package com.gradecalculator.service;

import com.gradecalculator.dto.*;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private GradeCalculationService gradeCalculationService;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(studentRepository, semesterRepository, enrollmentRepository, gradeCalculationService);
    }

    @Test
    void getStudentDashboardThrowsWhenStudentNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStudentDashboard(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student not found");
    }

    @Test
    void getStudentDashboardCalculatesAggregatesCorrectly() {
        Long studentId = 1L;
        Student student = new Student("John Doe", "CS2024001");
        student.setId(studentId);

        Semester sem1 = new Semester(1);
        sem1.setId(10L);
        Semester sem2 = new Semester(2);
        sem2.setId(20L);
        List<Semester> semesters = Arrays.asList(sem1, sem2);

        Course c1 = new Course("CS101", "Intro to CS", 4);
        Course c2 = new Course("CS102", "Data Structures", 4);
        Course c3 = new Course("CS201", "Algorithms", 4);

        Enrollment e1 = new Enrollment(student, c1, LetterGrade.O); // GP 10, CP 40
        Enrollment e2 = new Enrollment(student, c2, LetterGrade.B); // GP 6, CP 24
        Enrollment e3 = new Enrollment(student, c3, LetterGrade.F); // GP 0, CP 0
        Enrollment e4 = new Enrollment(student, new Course("CS202", "Ungraded", 3), null); // Ungraded

        List<Enrollment> allEnrollments = Arrays.asList(e1, e2, e3, e4);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(allEnrollments);
        
        CgpaResponse cgpaResponse = new CgpaResponse(studentId, 8.0, 8, 64, 2);
        when(gradeCalculationService.calculateOverallCGPA(studentId)).thenReturn(cgpaResponse);
        when(semesterRepository.findAll()).thenReturn(semesters);

        // Sem 1 enrollments: CS101 (O), CS102 (B)
        when(enrollmentRepository.findByStudentIdAndSemesterId(studentId, 10L)).thenReturn(Arrays.asList(e1, e2));
        // Sem 2 enrollments: CS201 (F), CS202 (null)
        when(enrollmentRepository.findByStudentIdAndSemesterId(studentId, 20L)).thenReturn(Arrays.asList(e3, e4));

        DashboardResponse result = service.getStudentDashboard(studentId);

        assertThat(result.getStudentId()).isEqualTo(studentId);
        assertThat(result.getStudentName()).isEqualTo("John Doe");
        assertThat(result.getStudentRoll()).isEqualTo("CS2024001");
        assertThat(result.getCgpa()).isEqualTo(8.0);
        assertThat(result.getTotalCredits()).isEqualTo(8);
        
        // Total courses excluding F grades = 3 (O, B, and the null ungraded course is also F-excluded but wait,
        // let's look at DashboardService.java:
        // response.setTotalCourses((int) enrollments.stream().filter(e -> e.getGrade() != LetterGrade.F).count());
        // e1 (O), e2 (B), e4 (null grade != LetterGrade.F) -> 3 courses.
        assertThat(result.getTotalCourses()).isEqualTo(3);
        assertThat(result.getSemestersCompleted()).isEqualTo(2);

        // Verify grade distribution
        Map<String, Integer> expectedGradeDist = new HashMap<>();
        expectedGradeDist.put("O", 1);
        expectedGradeDist.put("B", 1);
        expectedGradeDist.put("F", 1);
        assertThat(result.getGradeDistribution()).isEqualTo(expectedGradeDist);

        // Verify SGPA by semester
        // Sem 1 credits = 8, Sem 1 points = 64 -> SGPA = 8.0
        // Sem 2 credits = 4 (e3 is F, 4 credits), Sem 2 points = 0 -> SGPA = 0.0
        assertThat(result.getSgpaBySemester()).containsEntry("Sem 1", 8.0);
        assertThat(result.getSgpaBySemester()).containsEntry("Sem 2", 0.0);

        // Verify pass rate
        // Graded: O, B, F (3 graded)
        // Passed: O, B (2 passed)
        // Pass rate = 2 * 100 / 3 = 66
        assertThat(result.getPassRate()).isEqualTo(66);
        assertThat(result.getOutstandingCount()).isEqualTo(1); // Only c1 is O

        assertThat(result.getSemesterResults()).hasSize(2);
        SemesterResultResponse sr1 = result.getSemesterResults().get(0);
        assertThat(sr1.getSemesterNumber()).isEqualTo(1);
        assertThat(sr1.getSgpa()).isEqualTo(8.0);
        assertThat(sr1.getTotalCredits()).isEqualTo(8);
        assertThat(sr1.getCourses()).hasSize(2);
    }

    @Test
    void getSemesterResultThrowsWhenStudentNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSemesterResult(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student not found");
    }

    @Test
    void getSemesterResultThrowsWhenSemesterNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(new Student()));
        when(semesterRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSemesterResult(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester not found");
    }

    @Test
    void getSemesterResultReturnsRowsSortedByCourseCode() {
        Long studentId = 1L;
        Long semesterId = 10L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(new Semester()));

        Course c1 = new Course("CS102", "Data Structures", 4);
        Course c2 = new Course("CS101", "Intro to CS", 4);
        Course c3 = new Course("CS103", "Ungraded", 3);

        Enrollment e1 = new Enrollment(new Student(), c1, LetterGrade.A);
        Enrollment e2 = new Enrollment(new Student(), c2, LetterGrade.O);
        Enrollment e3 = new Enrollment(new Student(), c3, null);

        // Unordered list from repository
        when(enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId))
                .thenReturn(Arrays.asList(e1, e2, e3));

        List<SemesterResultRowResponse> result = service.getSemesterResult(studentId, semesterId);

        assertThat(result).hasSize(3);
        // Sorted: CS101, CS102, CS103
        assertThat(result.get(0).courseCode()).isEqualTo("CS101");
        assertThat(result.get(0).grade()).isEqualTo("O");
        assertThat(result.get(0).gradePoints()).isEqualTo(10);
        assertThat(result.get(0).creditPoints()).isEqualTo(40);

        assertThat(result.get(1).courseCode()).isEqualTo("CS102");
        assertThat(result.get(1).grade()).isEqualTo("A");
        assertThat(result.get(1).gradePoints()).isEqualTo(8);
        assertThat(result.get(1).creditPoints()).isEqualTo(32);

        assertThat(result.get(2).courseCode()).isEqualTo("CS103");
        assertThat(result.get(2).grade()).isNull();
        assertThat(result.get(2).gradePoints()).isZero();
        assertThat(result.get(2).creditPoints()).isZero();
    }
}
