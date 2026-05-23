package com.gradecalculator.service;

import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private UserService userService;
    @Mock private AppUserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(studentRepository, enrollmentRepository, userService, userRepository, passwordEncoder);
    }

    @Test
    void createsStudentInUpperCaseWithDobAsPassword() {
        String name = "Alice Johnson";
        String roll = "cs2024001"; // lowercase input
        String dob = "2004-01-01";

        when(studentRepository.findByStudentId(roll)).thenReturn(Optional.empty());
        
        Student savedStudent = new Student(name, roll, "Computer Science");
        savedStudent.setId(1L);
        savedStudent.setUsername("CS2024001");
        savedStudent.setDateOfBirth(dob);
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        Student response = service.create(name, roll, "Computer Science", dob);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("CS2024001"); // uppercase username!
        assertThat(response.getDateOfBirth()).isEqualTo("2004-01-01");

        // Verify that registration was initiated
        verify(userService).register(argThat(req -> 
            req.getUsername().equals("CS2024001") && req.getPassword().equals("2004-01-01")
        ));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void throwsOnDuplicateRollNumber() {
        String name = "Alice Johnson";
        String roll = "CS2024001";
        
        when(studentRepository.findByStudentId(roll)).thenReturn(Optional.of(new Student(name, roll)));

        assertThatThrownBy(() -> service.create(name, roll, "Computer Science", "2004-01-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A student with this roll number already exists");
    }

    @Test
    void throwsOnMissingDateOfBirth() {
        assertThatThrownBy(() -> service.create("Alice", "CS2024001", "Computer Science", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Date of birth is required");

        assertThatThrownBy(() -> service.create("Alice", "CS2024001", "Computer Science", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Date of birth is required");
    }

    @Test
    void updatesStudentDetailsAndSyncsAppUser() {
        Student existingStudent = new Student("Alice Johnson", "CS2024001");
        existingStudent.setId(1L);
        existingStudent.setUsername("CS2024001");
        existingStudent.setDateOfBirth("2004-01-01");

        AppUser existingUser = new AppUser();
        existingUser.setUsername("CS2024001");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.findByStudentId("CS2024001")).thenReturn(Optional.of(existingStudent));
        when(userRepository.findByUsername("CS2024001")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("2004-01-15")).thenReturn("encodedPassword");
        
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student response = service.update(1L, "Alice J. Smith", "CS2024001", "IT", "2004-01-15");

        assertThat(response.getName()).isEqualTo("Alice J. Smith");
        assertThat(response.getBranch()).isEqualTo("IT");
        assertThat(response.getDateOfBirth()).isEqualTo("2004-01-15");

        // Verify that matching user was updated
        verify(userRepository).save(argThat(user -> 
            user.getUsername().equals("CS2024001") &&
            user.getDepartment().equals("IT") &&
            user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void throwsOnDuplicateRollNumberDuringUpdate() {
        Student s1 = new Student("Alice", "CS2024001");
        s1.setId(1L);
        Student s2 = new Student("Bob", "CS2024002");
        s2.setId(2L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(s1));
        when(studentRepository.findByStudentId("CS2024002")).thenReturn(Optional.of(s2));

        assertThatThrownBy(() -> service.update(1L, "Alice", "CS2024002", "CS", "2004-01-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A student with this roll number already exists");
    }

    @Test
    void deletesStudentAndAssociatedAppUserAndEnrollments() {
        Student existingStudent = new Student("Alice Johnson", "CS2024001");
        existingStudent.setId(1L);
        existingStudent.setUsername("CS2024001");

        AppUser existingUser = new AppUser();
        existingUser.setUsername("CS2024001");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
        when(userRepository.findByUsername("CS2024001")).thenReturn(Optional.of(existingUser));
        
        List<Enrollment> enrollments = Collections.singletonList(new Enrollment());
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(enrollments);

        service.delete(1L);

        verify(userRepository).delete(existingUser);
        verify(enrollmentRepository).deleteAll(enrollments);
        verify(studentRepository).delete(existingStudent);
    }
}
