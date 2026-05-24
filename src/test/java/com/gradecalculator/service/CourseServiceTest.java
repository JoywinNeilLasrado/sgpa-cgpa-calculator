package com.gradecalculator.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private AppUserRepository userRepository;

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(courseRepository, semesterRepository, enrollmentRepository, userRepository);
    }

    @Test
    void findAllReturnsAllCourses() {
        List<Course> courses = Arrays.asList(new Course("CS101", "Intro to CS", 4), new Course("CS102", "Data Structures", 4));
        when(courseRepository.findAll()).thenReturn(courses);

        List<Course> result = service.findAll();

        assertThat(result).hasSize(2);
        verify(courseRepository).findAll();
    }

    @Test
    void findBySemesterIdReturnsSemesterCourses() {
        List<Course> courses = Collections.singletonList(new Course("CS101", "Intro to CS", 4));
        when(courseRepository.findBySemesterId(1L)).thenReturn(courses);

        List<Course> result = service.findBySemesterId(1L);

        assertThat(result).hasSize(1);
        verify(courseRepository).findBySemesterId(1L);
    }

    @Test
    void findByIdReturnsCourseOptional() {
        Course course = new Course("CS101", "Intro to CS", 4);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        Optional<Course> result = service.findById(1L);

        assertThat(result).isPresent().contains(course);
        verify(courseRepository).findById(1L);
    }

    @Test
    void findByFacultyIdReturnsFacultyCourses() {
        List<Course> courses = Collections.singletonList(new Course("CS101", "Intro to CS", 4));
        when(courseRepository.findByFacultyId(2L)).thenReturn(courses);

        List<Course> result = service.findByFacultyId(2L);

        assertThat(result).hasSize(1);
        verify(courseRepository).findByFacultyId(2L);
    }

    @Test
    void createsCourseSuccessfullyWithoutFaculty() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findByCourseCodeAndSemesterId("CS101", 1L)).thenReturn(Optional.empty());

        Course course = new Course("CS101", "Intro to CS", 4);
        course.setSemester(semester);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        Course result = service.create("CS101", "Intro to CS", "4", "1", null);

        assertThat(result.getCourseCode()).isEqualTo("CS101");
        assertThat(result.getCourseName()).isEqualTo("Intro to CS");
        assertThat(result.getCredits()).isEqualTo(4);
        assertThat(result.getSemester()).isEqualTo(semester);
        assertThat(result.getFaculty()).isNull();

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createsCourseSuccessfullyWithFaculty() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        AppUser faculty = new AppUser();
        faculty.setId(2L);
        faculty.setRole(AppUser.Role.FACULTY);

        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findByCourseCodeAndSemesterId("CS101", 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(faculty));

        Course course = new Course("CS101", "Intro to CS", 4);
        course.setSemester(semester);
        course.setFaculty(faculty);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        Course result = service.create("CS101", "Intro to CS", 4, 1, 2);

        assertThat(result.getFaculty()).isEqualTo(faculty);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createThrowsWhenSemesterIdInvalid() {
        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", 4, "invalid", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester is required");
    }

    @Test
    void createThrowsWhenSemesterNotFound() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", 4, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semester not found");
    }

    @Test
    void createThrowsWhenCourseCodeOrNameIsEmpty() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));

        assertThatThrownBy(() -> service.create("", "Intro to CS", 4, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course code is required");

        assertThatThrownBy(() -> service.create("CS101", "   ", 4, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course name is required");
    }

    @Test
    void createThrowsWhenCreditsInvalid() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));

        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", "0", 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credits must be greater than zero");

        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", "-2", 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credits must be greater than zero");

        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", "invalid", 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credits must be greater than zero");
    }

    @Test
    void createThrowsWhenCourseAlreadyExistsInSemester() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findByCourseCodeAndSemesterId("CS101", 1L)).thenReturn(Optional.of(new Course()));

        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", 4, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A course with this code already exists in the semester");
    }

    @Test
    void createThrowsWhenFacultyNotFound() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findByCourseCodeAndSemesterId("CS101", 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", 4, 1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Faculty user not found");
    }

    @Test
    void createThrowsWhenUserIsNotFaculty() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        AppUser student = new AppUser();
        student.setId(2L);
        student.setRole(AppUser.Role.STUDENT);

        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findByCourseCodeAndSemesterId("CS101", 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> service.create("CS101", "Intro to CS", 4, 1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assigned user must be a faculty member");
    }

    @Test
    void updatesCourseSuccessfully() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        Course existing = new Course("CS101", "Old Name", 3);
        existing.setId(10L);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findByCourseCodeAndSemesterId("CS101", 1L)).thenReturn(Optional.of(existing));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course result = service.update(10L, "CS101", "New Name", 4, 1L, null);

        assertThat(result.getCourseName()).isEqualTo("New Name");
        assertThat(result.getCredits()).isEqualTo(4);
        verify(courseRepository).save(existing);
    }

    @Test
    void updateThrowsWhenCourseNotFound() {
        when(courseRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(10L, "CS101", "Name", 4, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course not found");
    }

    @Test
    void updateThrowsWhenCourseCodeDuplicateInSemester() {
        Semester semester = new Semester(1);
        semester.setId(1L);
        Course existing = new Course("CS101", "Name", 3);
        existing.setId(10L);
        Course other = new Course("CS101", "Other", 4);
        other.setId(11L);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(courseRepository.findByCourseCodeAndSemesterId("CS101", 1L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(10L, "CS101", "Name", 4, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A course with this code already exists in the semester");
    }

    @Test
    void deletesCourseAndAssociatedEnrollments() {
        Course course = new Course("CS101", "Intro to CS", 4);
        course.setId(10L);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        List<Enrollment> enrollments = Arrays.asList(new Enrollment(), new Enrollment());
        when(enrollmentRepository.findByCourseId(10L)).thenReturn(enrollments);

        service.delete(10L);

        verify(enrollmentRepository).deleteAll(enrollments);
        verify(courseRepository).delete(course);
    }
}
