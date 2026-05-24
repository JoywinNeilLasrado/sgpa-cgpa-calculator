package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.model.Student;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradecalculator.config.SecurityConfig;
import com.gradecalculator.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = StudentController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private StudentService studentService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal facultyPrincipal;

    @BeforeEach
    void setUp() {
        com.gradecalculator.model.AppUser admin = new com.gradecalculator.model.AppUser();
        admin.setUsername("admin");
        admin.setRole(com.gradecalculator.model.AppUser.Role.ADMIN);
        adminPrincipal = new UserPrincipal(admin);

        com.gradecalculator.model.AppUser faculty = new com.gradecalculator.model.AppUser();
        faculty.setUsername("faculty");
        faculty.setRole(com.gradecalculator.model.AppUser.Role.FACULTY);
        facultyPrincipal = new UserPrincipal(faculty);
    }

    @Test
    void getAllStudentsReturnsListForAdminAndFaculty() throws Exception {
        List<Student> students = Arrays.asList(new Student("Alice", "CS01"), new Student("Bob", "CS02"));
        when(studentService.findAll()).thenReturn(students);

        mockMvc.perform(get("/api/students")
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getStudentSucceedsForOwner() throws Exception {
        Long studentId = 1L;
        Student student = new Student("Alice", "CS01");
        student.setId(studentId);

        when(studentService.findById(studentId)).thenReturn(Optional.of(student));
        when(securityExpressionEvaluator.isStudentOwner(any(UserPrincipal.class), eq(studentId))).thenReturn(true);

        mockMvc.perform(get("/api/students/{id}", studentId)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.studentId").value("CS01"));
    }

    @Test
    void getStudentReturnsNotFoundWhenAbsent() throws Exception {
        Long studentId = 1L;
        when(studentService.findById(studentId)).thenReturn(Optional.empty());
        when(securityExpressionEvaluator.isStudentOwner(any(UserPrincipal.class), eq(studentId))).thenReturn(true);

        mockMvc.perform(get("/api/students/{id}", studentId)
                        .with(user(adminPrincipal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createStudentSucceedsForAdmin() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Alice");
        requestBody.put("studentId", "CS2024001");
        requestBody.put("branch", "Computer Science");
        requestBody.put("dateOfBirth", "2004-01-01");

        Student created = new Student("Alice", "CS2024001", "Computer Science");
        created.setId(1L);

        when(studentService.create("Alice", "CS2024001", "Computer Science", "2004-01-01")).thenReturn(created);

        mockMvc.perform(post("/api/students")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void createStudentThrowsOnInvalidFields() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", ""); // empty name
        requestBody.put("studentId", "CS2024001");
        requestBody.put("branch", "Computer Science");

        mockMvc.perform(post("/api/students")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest()); // Handled by IllegalArgumentException controller advice
    }

    @Test
    void updateStudentSucceedsForAdmin() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Alice Edited");
        requestBody.put("studentId", "CS2024001");
        requestBody.put("branch", "IT");
        requestBody.put("dateOfBirth", "2004-01-01");

        Student updated = new Student("Alice Edited", "CS2024001", "IT");
        updated.setId(1L);

        when(studentService.update(1L, "Alice Edited", "CS2024001", "IT", "2004-01-01")).thenReturn(updated);

        mockMvc.perform(put("/api/students/{id}", 1L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Edited"))
                .andExpect(jsonPath("$.branch").value("IT"));
    }

    @Test
    void deleteStudentSucceedsForAdmin() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", 1L)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());

        verify(studentService).delete(1L);
    }
}
