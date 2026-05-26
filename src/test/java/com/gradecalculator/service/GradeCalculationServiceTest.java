package com.gradecalculator.service;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.model.Semester;
import com.gradecalculator.model.Student;
import com.gradecalculator.repository.EnrollmentRepository;
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



    private GradeCalculationService service;
    private Student student;

    @BeforeEach
    void setUp() {
        service = new GradeCalculationService(studentRepository, enrollmentRepository);

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

    @Test
    void throwsWhenStudentNotFoundForSgpa() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculateSGPA(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student not found");
    }

    @Test
    void throwsWhenStudentNotFoundForCgpa() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculateCGPA(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student not found");
    }

    @Test
    void calculatesCgpaUpToSemester() {
        Semester sem1 = semester(1L, 1);
        Semester sem2 = semester(2L, 2);
        Semester sem3 = semester(3L, 3);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.O), // Sem 1
                enrollment(course(2L, "CS201", 4, sem2), LetterGrade.A), // Sem 2
                enrollment(course(3L, "CS301", 4, sem3), LetterGrade.B)  // Sem 3 (should be ignored since we only calculate up to Sem 2)
        ));

        CgpaResponse response = service.calculateCGPA(1L, 2L);

        assertThat(response.getCgpa()).isEqualTo(9.0); // (4*10 + 4*9) / 8 = 9.0
        assertThat(response.getTotalEarnedCredits()).isEqualTo(8);
        assertThat(response.getTotalCreditPoints()).isEqualTo(72);
        assertThat(response.getSemestersCompleted()).isEqualTo(2);
    }

    @Test
    void returnsZeroCgpaWhenNoCreditsEarned() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of());

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getCgpa()).isEqualTo(0.0);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(0);
    }

    @Test
    void handlesUngradedCoursesInSgpa() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, semester(1L, 1)), LetterGrade.A), // 4 * 8 = 32 points
                enrollment(course(2L, "MA101", 4, semester(1L, 1)), null)           // ungraded: should be ignored in calculations
        ));

        SgpaResponse response = service.calculateSGPA(1L, 1L);

        assertThat(response.getSgpa()).isEqualTo(8.0);
        assertThat(response.getTotalCredits()).isEqualTo(4);
        assertThat(response.getTotalCreditPoints()).isEqualTo(32);
    }

    @Test
    void handlesUngradedCoursesInCgpa() {
        Semester sem1 = semester(1L, 1);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.O), // 4 * 10 = 40
                enrollment(course(2L, "MA101", 4, sem1), null)           // ungraded: ignored
        ));

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getCgpa()).isEqualTo(10.0);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(4);
        assertThat(response.getTotalCreditPoints()).isEqualTo(40);
    }

    @Test
    void calculateOverallCgpaWithFGrades() {
        Semester sem1 = semester(1L, 1);
        Semester sem2 = semester(2L, 2);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.O),  // 4 * 10 = 40
                enrollment(course(2L, "MA101", 4, sem1), LetterGrade.F),  // F grade: excluded
                enrollment(course(3L, "CS201", 4, sem2), LetterGrade.A)   // 4 * 8 = 32
        ));

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getCgpa()).isEqualTo(9.0); // (40 + 32) / (4 + 4) = 9.0
        assertThat(response.getTotalEarnedCredits()).isEqualTo(8);
        assertThat(response.getTotalCreditPoints()).isEqualTo(72);
        assertThat(response.getSemestersCompleted()).isEqualTo(2);
    }

    @Test
    void calculateOverallCgpaWithOnlyFGrades() {
        Semester sem1 = semester(1L, 1);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.F),
                enrollment(course(2L, "MA101", 4, sem1), LetterGrade.F)
        ));

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getCgpa()).isEqualTo(0.0);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(0);
        assertThat(response.getTotalCreditPoints()).isEqualTo(0);
    }

    @Test
    void calculateCgpaUpToSemesterWithOnlyFGrades() {
        Semester sem1 = semester(1L, 1);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.F)
        ));

        CgpaResponse response = service.calculateCGPA(1L, 1L);

        assertThat(response.getCgpa()).isEqualTo(0.0);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(0);
        assertThat(response.getTotalCreditPoints()).isEqualTo(0);
        assertThat(response.getSemestersCompleted()).isEqualTo(0);
    }

    @Test
    void calculateSgpaWithAllFGrades() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, semester(1L, 1)), LetterGrade.F),
                enrollment(course(2L, "MA101", 4, semester(1L, 1)), LetterGrade.F)
        ));

        SgpaResponse response = service.calculateSGPA(1L, 1L);

        assertThat(response.getSgpa()).isEqualTo(0.0);
        assertThat(response.getTotalCredits()).isEqualTo(8);
        assertThat(response.getTotalCreditPoints()).isEqualTo(0);
    }

    @Test
    void calculateSgpaWithSingleCourse() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 3, semester(1L, 1)), LetterGrade.B) // 3 * 6 = 18 points
        ));

        SgpaResponse response = service.calculateSGPA(1L, 1L);

        assertThat(response.getSgpa()).isEqualTo(6.0);
        assertThat(response.getTotalCredits()).isEqualTo(3);
        assertThat(response.getTotalCreditPoints()).isEqualTo(18);
    }

    @Test
    void calculateSgpaWithMixedGradesAndNulls() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndSemesterId(1L, 1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, semester(1L, 1)), LetterGrade.O), // 4 * 10 = 40
                enrollment(course(2L, "MA101", 4, semester(1L, 1)), LetterGrade.F), // 4 * 0 = 0
                enrollment(course(3L, "PH101", 4, semester(1L, 1)), null)           // ungraded: ignored
        ));

        SgpaResponse response = service.calculateSGPA(1L, 1L);

        assertThat(response.getSgpa()).isEqualTo(5.0); // (40 + 0) / (4 + 4) = 5.0
        assertThat(response.getTotalCredits()).isEqualTo(8);
        assertThat(response.getTotalCreditPoints()).isEqualTo(40);
    }

    @Test
    void calculateCgpaWithAllO() {
        Semester sem1 = semester(1L, 1);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.O),
                enrollment(course(2L, "MA101", 4, sem1), LetterGrade.O)
        ));

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getCgpa()).isEqualTo(10.0);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(8);
        assertThat(response.getTotalCreditPoints()).isEqualTo(80);
    }

    @Test
    void calculateCgpaWithAllP() {
        Semester sem1 = semester(1L, 1);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.P), // 4 * 4 = 16
                enrollment(course(2L, "MA101", 2, sem1), LetterGrade.P)  // 2 * 4 = 8
        ));

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getCgpa()).isEqualTo(4.0);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(6);
        assertThat(response.getTotalCreditPoints()).isEqualTo(24);
    }

    @Test
    void calculateOverallCgpaWithSemesterCompletedCount() {
        Semester sem1 = semester(1L, 1);
        Semester sem2 = semester(2L, 2);
        Semester sem3 = semester(3L, 3);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.A),
                enrollment(course(2L, "CS201", 4, sem2), LetterGrade.A),
                enrollment(course(3L, "CS301", 4, sem3), LetterGrade.A)
        ));

        CgpaResponse response = service.calculateOverallCGPA(1L);

        assertThat(response.getSemestersCompleted()).isEqualTo(3);
    }

    @Test
    void calculateCgpaUpToSemesterIgnoringLaterSemesters() {
        Semester sem1 = semester(1L, 1);
        Semester sem2 = semester(2L, 2);
        Semester sem3 = semester(3L, 3);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(
                enrollment(course(1L, "CS101", 4, sem1), LetterGrade.O), // Sem 1
                enrollment(course(2L, "CS201", 4, sem2), LetterGrade.O), // Sem 2
                enrollment(course(3L, "CS301", 4, sem3), LetterGrade.O)  // Sem 3 (ignored for limit = 2)
        ));

        CgpaResponse response = service.calculateCGPA(1L, 2L);

        assertThat(response.getSemestersCompleted()).isEqualTo(2);
        assertThat(response.getTotalEarnedCredits()).isEqualTo(8);
        assertThat(response.getTotalCreditPoints()).isEqualTo(80);
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
