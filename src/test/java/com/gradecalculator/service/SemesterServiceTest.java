package com.gradecalculator.service;

import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.Semester;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    @Mock private SemesterRepository semesterRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    private SemesterService service;

    @BeforeEach
    void setUp() {
        service = new SemesterService(semesterRepository, courseRepository, enrollmentRepository);
    }

    @Test
    void findAllReturnsAllSemesters() {
        List<Semester> semesters = Arrays.asList(new Semester(1), new Semester(2));
        when(semesterRepository.findAll()).thenReturn(semesters);

        List<Semester> result = service.findAll();

        assertThat(result).hasSize(2);
        verify(semesterRepository).findAll();
    }

    @Test
    void findByIdReturnsSemesterOptional() {
        Semester semester = new Semester(1);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));

        Optional<Semester> result = service.findById(1L);

        assertThat(result).isPresent().contains(semester);
        verify(semesterRepository).findById(1L);
    }

    @Test
    void createsSemesterSuccessfully() {
        Semester semester = new Semester(1);
        when(semesterRepository.save(any(Semester.class))).thenReturn(semester);

        Semester result = service.create(1);

        assertThat(result.getSemesterNumber()).isEqualTo(1);
        verify(semesterRepository).save(any(Semester.class));
    }

    @Test
    void createThrowsWhenSemesterNumberInvalid() {
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester number must be greater than zero");

        assertThatThrownBy(() -> service.create(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester number must be greater than zero");

        assertThatThrownBy(() -> service.create(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester number must be greater than zero");
    }

    @Test
    void updatesSemesterSuccessfully() {
        Semester existing = new Semester(1);
        existing.setId(1L);

        when(semesterRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(semesterRepository.save(any(Semester.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Semester result = service.update(1L, 2);

        assertThat(result.getSemesterNumber()).isEqualTo(2);
        verify(semesterRepository).save(existing);
    }

    @Test
    void updateThrowsWhenSemesterNotFound() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester not found");
    }

    @Test
    void updateThrowsWhenSemesterNumberInvalid() {
        Semester existing = new Semester(1);
        existing.setId(1L);

        when(semesterRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester number must be greater than zero");
    }

    @Test
    void deletesSemesterCascadingCoursesAndEnrollments() {
        Semester semester = new Semester(1);
        semester.setId(1L);

        Course c1 = new Course("CS101", "Intro", 3);
        c1.setId(10L);
        Course c2 = new Course("CS102", "DS", 4);
        c2.setId(11L);
        List<Course> courses = Arrays.asList(c1, c2);

        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findBySemesterId(1L)).thenReturn(courses);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setId(100L);
        List<Enrollment> e1 = Collections.singletonList(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setId(200L);
        List<Enrollment> e2 = Collections.singletonList(enrollment2);

        when(enrollmentRepository.findByCourseId(10L)).thenReturn(e1);
        when(enrollmentRepository.findByCourseId(11L)).thenReturn(e2);

        service.delete(1L);

        verify(enrollmentRepository).deleteAll(e1);
        verify(enrollmentRepository).deleteAll(e2);
        verify(courseRepository).deleteAll(courses);
        verify(semesterRepository).delete(semester);
    }

    @Test
    void deleteThrowsWhenSemesterNotFound() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester not found");
    }
}
