package com.gradecalculator.service;

import com.gradecalculator.model.Department;
import com.gradecalculator.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentService service;

    @BeforeEach
    void setUp() {
        service = new DepartmentService(departmentRepository);
    }

    @Test
    void findAllReturnsAllDepartments() {
        List<Department> departments = Arrays.asList(
                new Department("Computer Science", "CS"),
                new Department("Information Technology", "IT")
        );
        when(departmentRepository.findAll()).thenReturn(departments);

        List<Department> result = service.findAll();

        assertThat(result).hasSize(2);
        verify(departmentRepository).findAll();
    }

    @Test
    void findByIdReturnsDepartmentWhenExists() {
        Department department = new Department("Computer Science", "CS");
        department.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        Optional<Department> result = service.findById(1L);

        assertThat(result).isPresent().contains(department);
        verify(departmentRepository).findById(1L);
    }

    @Test
    void findByIdReturnsEmptyWhenNotExists() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Department> result = service.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void createDepartmentSuccessfully() {
        when(departmentRepository.findByName("Computer Science")).thenReturn(Optional.empty());
        when(departmentRepository.findByCode("CS")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        Department result = service.create("Computer Science", "cs");

        assertThat(result.getName()).isEqualTo("COMPUTER SCIENCE");
        assertThat(result.getCode()).isEqualTo("CS");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createThrowsWhenNameIsNull() {
        assertThatThrownBy(() -> service.create(null, "CS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department name cannot be empty");
    }

    @Test
    void createThrowsWhenNameIsEmpty() {
        assertThatThrownBy(() -> service.create("   ", "CS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department name cannot be empty");
    }

    @Test
    void createThrowsWhenCodeIsNull() {
        assertThatThrownBy(() -> service.create("Computer Science", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department code cannot be empty");
    }

    @Test
    void createThrowsWhenCodeIsEmpty() {
        assertThatThrownBy(() -> service.create("Computer Science", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department code cannot be empty");
    }

    @Test
    void createThrowsWhenNameAlreadyExists() {
        Department existing = new Department("Computer Science", "CS");
        when(departmentRepository.findByName("COMPUTER SCIENCE")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create("Computer Science", "XX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department with name 'COMPUTER SCIENCE' already exists");
    }

    @Test
    void createThrowsWhenCodeAlreadyExists() {
        Department existing = new Department("Other", "CS");
        when(departmentRepository.findByName("Computer Science")).thenReturn(Optional.empty());
        when(departmentRepository.findByCode("CS")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create("Computer Science", "cs"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department with code 'CS' already exists");
    }

    @Test
    void updateDepartmentSuccessfully() {
        Department existing = new Department("Computer Science", "CS");
        existing.setId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("Software Engineering")).thenReturn(Optional.empty());
        when(departmentRepository.findByCode("SE")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department result = service.update(1L, "Software Engineering", "se");

        assertThat(result.getName()).isEqualTo("SOFTWARE ENGINEERING");
        assertThat(result.getCode()).isEqualTo("SE");
        verify(departmentRepository).save(existing);
    }

    @Test
    void updateThrowsWhenDepartmentNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, "Name", "XX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department not found");
    }

    @Test
    void updateThrowsWhenNameConflictsWithAnother() {
        Department existing = new Department("Computer Science", "CS");
        existing.setId(1L);
        Department other = new Department("Computer Science", "XX");
        other.setId(2L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("COMPUTER SCIENCE")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(1L, "Computer Science", "YY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Another department with name 'COMPUTER SCIENCE' already exists");
    }

    @Test
    void updateThrowsWhenCodeConflictsWithAnother() {
        Department existing = new Department("Computer Science", "CS");
        existing.setId(1L);
        Department other = new Department("XX", "CS");
        other.setId(2L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("Computer Science")).thenReturn(Optional.empty());
        when(departmentRepository.findByCode("CS")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(1L, "Computer Science", "cs"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Another department with code 'CS' already exists");
    }

    @Test
    void deleteDepartmentSuccessfully() {
        Department department = new Department("Computer Science", "CS");
        department.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        service.delete(1L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteThrowsWhenDepartmentNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Department not found");
    }
}
