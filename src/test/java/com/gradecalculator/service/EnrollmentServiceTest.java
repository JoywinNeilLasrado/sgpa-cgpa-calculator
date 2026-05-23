package com.gradecalculator.service;

import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.EnrollmentResponse;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private EnrollmentService service;
    private Student student;
    private Course course;
    private Semester semester;

    @BeforeEach
    void setUp() {
        service = new EnrollmentService(enrollmentRepository, studentRepository, courseRepository);
        SecurityContextHolder.setContext(securityContext);

        student = new Student("Alice Johnson", "CS2024001");
        student.setId(1L);

        semester = new Semester(1);
        semester.setId(10L);

        course = new Course("CS101", "Programming", 4);
        course.setId(2L);
        course.setSemester(semester);
    }

    @Test
    void createsEnrollmentForAdminWithGrade() {
        // Setup authentication as ADMIN
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setGrade("A+");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(false);

        Enrollment savedEnrollment = new Enrollment(student, course, LetterGrade.A_PLUS);
        savedEnrollment.setId(100L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);

        EnrollmentResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.grade()).isEqualTo("A+");
        assertThat(response.gradePoints()).isEqualTo(9);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void createsEnrollmentForStudentForcesNullGrade() {
        // Setup authentication as STUDENT (testing Grade Injection Protection)
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .when(authentication).getAuthorities();

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setGrade("O"); // Injecting grade 'O'

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(false);

        // Saved enrollment grade must be null (forced ungraded status)
        Enrollment savedEnrollment = new Enrollment(student, course, null);
        savedEnrollment.setId(100L);
        when(enrollmentRepository.save(argThat(e -> e.getGrade() == null))).thenReturn(savedEnrollment);

        EnrollmentResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.grade()).isNull(); // Grade successfully set to null!
        assertThat(response.gradePoints()).isEqualTo(0);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void throwsWhenEnrollmentAlreadyExists() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This student is already enrolled in this course");
    }

    @Test
    void throwsWhenStudentNotFound() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(99L);

        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student not found");
    }

    @Test
    void throwsWhenCourseNotFound() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setCourseId(99L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course not found");
    }

    @Test
    void toEnrollmentResponseMapsNullGradeSafely() {
        Enrollment enrollment = new Enrollment(student, course, null); // Ungraded course
        enrollment.setId(100L);

        EnrollmentResponse response = service.toEnrollmentResponse(enrollment);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.grade()).isNull();
        assertThat(response.gradePoints()).isEqualTo(0);
        assertThat(response.creditPoints()).isEqualTo(0);
    }

    @Test
    void updatesEnrollmentGradeSuccessfully() {
        Enrollment enrollment = new Enrollment(student, course, null);
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment updated = service.updateGrade(100L, LetterGrade.A, "prof.jones");

        assertThat(updated.getGrade()).isEqualTo(LetterGrade.A);
        assertThat(updated.getLastModifiedBy()).isEqualTo("prof.jones");
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void deletesEnrollmentSuccessfully() {
        Enrollment enrollment = new Enrollment(student, course, LetterGrade.B);
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));

        service.delete(100L);

        verify(enrollmentRepository).delete(enrollment);
    }
}
