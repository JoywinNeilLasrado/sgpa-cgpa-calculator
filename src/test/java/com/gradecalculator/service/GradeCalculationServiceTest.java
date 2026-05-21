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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeCalculationServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private SemesterRepository semesterRepository;

    private GradeCalculationService service;
    private Student student;

    @BeforeEach
    void setUp() {
        service = new GradeCalculationService();
        ReflectionTestUtils.setField(service, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(service, "enrollmentRepository", enrollmentRepository);
        ReflectionTestUtils.setField(service, "semesterRepository", semesterRepository);

        student = new Student("John Doe", "CS2024001");
        student.setId(1L);
    }

    @Test
    void calculatesSgpaForNormalGrades() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, semester(1L, 1)), LetterGrade.A),
                enrollment(course(2L, "MA101", 3, semester(1L, 1)), LetterGrade.B_PLUS)
        ));

        SgpaResponse response = service.calculateSGPA(1L, 1L);

        assertThat(response.getSgpa()).isEqualTo(7.57);
        assertThat(response.getTotalCredits()).isEqualTo(7);
        assertThat(response.getTotalCreditPoints()).isEqualTo(53);
    }

    @Test
    void includesFGradesInSgpa() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, semester(1L, 1)), LetterGrade.A),
                enrollment(course(2L, "MA101", 4, semester(1L, 1)), LetterGrade.F)
        ));

        SgpaResponse response = service.calculateSGPA(1L, 1L);

        assertThat(response.getSgpa()).isEqualTo(4.0);
        assertThat(response.getTotalCredits()).isEqualTo(8);
        assertThat(response.getTotalCreditPoints()).isEqualTo(32);
    }

    @Test
    void excludesFGradesFromCgpa() {
        Semester firstSemester = semester(1L, 1);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, firstSemester), LetterGrade.A),
                enrollment(course(2L, "MA101", 4, firstSemester), LetterGrade.F)
        ));

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getCgpa()).isEqualTo(8.0);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(4);
        assertThat(response.getTotalCreditPoints()).isEqualTo(32);
        assertThat(response.getSemestersCompleted()).isEqualTo(1);
    }

    @Test
    void throwsWhenNoEnrollmentsExistForSgpa() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.calculateSGPA(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No enrollments found for this student in the specified semester");
    }

    @Test
    void roundsSgpaToTwoDecimals() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 2, semester(1L, 1)), LetterGrade.A),
                enrollment(course(2L, "MA101", 1, semester(1L, 1)), LetterGrade.C)
        ));

        SgpaResponse response = service.calculateSGPA(1L, 1L);

        assertThat(response.getSgpa()).isEqualTo(7.0);
    }

    private Enrollment enrollment(Course course, LetterGrade grade) {
        return new Enrollment(student, course, grade);
    }

    private Course course(Long id, String code, int credits, Semester semester) {
        Course course = new Course(code, code + " Name", credits);
        course.setId(id);
        course.setSemester(semester);
        return course;
    }

    private Semester semester(Long id, int number) {
        Semester semester = new Semester(number);
        semester.setId(id);
        return semester;
    }
}
