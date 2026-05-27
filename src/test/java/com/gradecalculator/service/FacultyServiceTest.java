package com.gradecalculator.service;

import com.gradecalculator.model.AppUser;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Semester;
import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.repository.CourseRepository;
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
class FacultyServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    private FacultyService service;

    @BeforeEach
    void setUp() {
        service = new FacultyService(userRepository, courseRepository);
    }

    @Test
    void findAllFacultyMembersReturnsOnlyFacultyUsers() {
        AppUser faculty1 = new AppUser();
        faculty1.setId(1L);
        faculty1.setRole(AppUser.Role.FACULTY);
        
        AppUser faculty2 = new AppUser();
        faculty2.setId(2L);
        faculty2.setRole(AppUser.Role.FACULTY);
        
        List<AppUser> facultyList = Arrays.asList(faculty1, faculty2);
        when(userRepository.findByRole(AppUser.Role.FACULTY)).thenReturn(facultyList);

        List<AppUser> result = service.findAllFacultyMembers();

        assertThat(result).hasSize(2);
        verify(userRepository).findByRole(AppUser.Role.FACULTY);
    }

    @Test
    void findAllFacultyMembersReturnsEmptyWhenNoFaculty() {
        when(userRepository.findByRole(AppUser.Role.FACULTY)).thenReturn(Collections.emptyList());

        List<AppUser> result = service.findAllFacultyMembers();

        assertThat(result).isEmpty();
    }

    @Test
    void deleteFacultyMemberRemovesFacultyAndNullifiesCourseAssignments() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        
        Course course1 = new Course("CS101", "Intro to CS", 4);
        course1.setFaculty(faculty);
        
        Course course2 = new Course("CS102", "Data Structures", 4);
        course2.setFaculty(faculty);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(courseRepository.findByFacultyId(1L)).thenReturn(Arrays.asList(course1, course2));

        service.deleteFacultyMember(1L);

        verify(courseRepository, times(2)).save(any(Course.class));
        verify(userRepository).delete(faculty);
    }

    @Test
    void deleteFacultyMemberWithNoAssignedCourses() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(courseRepository.findByFacultyId(1L)).thenReturn(Collections.emptyList());

        service.deleteFacultyMember(1L);

        verify(courseRepository, never()).save(any(Course.class));
        verify(userRepository).delete(faculty);
    }

    @Test
    void deleteFacultyMemberThrowsWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFacultyMember(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Faculty member not found");
    }

    @Test
    void assignFacultyToCourseSuccessfully() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        
        Semester semester = new Semester(1);
        Course course = new Course("CS101", "Intro to CS", 4);
        course.setId(10L);
        course.setSemester(semester);
        
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.assignFacultyToCourse(10L, 1L);

        verify(courseRepository).save(course);
    }

    @Test
    void assignFacultyToCourseThrowsWhenCourseNotFound() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignFacultyToCourse(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course not found");
    }

    @Test
    void assignFacultyToCourseThrowsWhenFacultyNotFound() {
        Course course = new Course("CS101", "Intro to CS", 4);
        course.setId(10L);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignFacultyToCourse(10L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Faculty member not found");
    }

    @Test
    void updateFacultyMemberSuccessfully() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        faculty.setName("Old Name");
        faculty.setUsername("olduser");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = service.updateFacultyMember(1L, "New Name", "newuser", "new@email.com", "Computer Science");

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@email.com");
        assertThat(result.getDepartment()).isEqualTo("Computer Science");
        verify(userRepository).save(faculty);
    }

    @Test
    void updateFacultyMemberKeepsSameUsernameWithoutConflict() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        faculty.setUsername("sameuser");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = service.updateFacultyMember(1L, "New Name", "sameuser", null, null);

        verify(userRepository, never()).existsByUsername(any());
        assertThat(result.getUsername()).isEqualTo("sameuser");
    }

    @Test
    void updateFacultyMemberThrowsWhenFacultyNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateFacultyMember(999L, "Name", "user", "email@test.com", "CS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Faculty member not found");
    }

    @Test
    void updateFacultyMemberThrowsWhenUsernameAlreadyTaken() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        faculty.setUsername("olduser");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        assertThatThrownBy(() -> service.updateFacultyMember(1L, "Name", "takenuser", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void updateFacultyMemberThrowsWhenNameIsEmpty() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));

        assertThatThrownBy(() -> service.updateFacultyMember(1L, "   ", "user", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Faculty name cannot be empty");
    }

    @Test
    void updateFacultyMemberThrowsWhenUsernameIsEmpty() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));

        assertThatThrownBy(() -> service.updateFacultyMember(1L, "Name", "", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be empty");
    }

    @Test
    void updateFacultyMemberTrimsWhitespace() {
        AppUser faculty = new AppUser();
        faculty.setId(1L);
        faculty.setRole(AppUser.Role.FACULTY);
        faculty.setUsername("user");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = service.updateFacultyMember(1L, "  Trimmed Name  ", "  trimmeduser  ", "  email@test.com  ", "  CS  ");

        assertThat(result.getName()).isEqualTo("Trimmed Name");
        assertThat(result.getUsername()).isEqualTo("trimmeduser");
        assertThat(result.getEmail()).isEqualTo("email@test.com");
        assertThat(result.getDepartment()).isEqualTo("CS");
    }
}
