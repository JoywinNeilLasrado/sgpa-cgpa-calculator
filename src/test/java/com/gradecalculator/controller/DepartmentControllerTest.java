package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.model.Department;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
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

@WebMvcTest(controllers = DepartmentController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private DepartmentService departmentService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal studentPrincipal;

    @BeforeEach
    void setUp() {
        com.gradecalculator.model.AppUser admin = new com.gradecalculator.model.AppUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(com.gradecalculator.model.AppUser.Role.ADMIN);
        adminPrincipal = new UserPrincipal(admin);

        com.gradecalculator.model.AppUser student = new com.gradecalculator.model.AppUser();
        student.setId(3L);
        student.setUsername("student");
        student.setRole(com.gradecalculator.model.AppUser.Role.STUDENT);
        studentPrincipal = new UserPrincipal(student);

        reset(departmentService);
    }

    @Test
    void getAllDepartmentsPermittedForAuthenticated() throws Exception {
        Department d1 = new Department("Computer Science", "CS");
        d1.setId(10L);
        when(departmentService.findAll()).thenReturn(Collections.singletonList(d1));

        mockMvc.perform(get("/api/departments")
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CS"));
    }

    @Test
    void getDepartmentByIdReturnsDepartment() throws Exception {
        Department d1 = new Department("Computer Science", "CS");
        d1.setId(10L);
        when(departmentService.findById(10L)).thenReturn(Optional.of(d1));
        when(departmentService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/departments/{id}", 10L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        mockMvc.perform(get("/api/departments/{id}", 99L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDepartmentValidatesInputAndEnforcesAdmin() throws Exception {
        Department d1 = new Department("Computer Science", "CS");
        d1.setId(10L);
        when(departmentService.create("Computer Science", "CS")).thenReturn(d1);

        Map<String, String> validRequest = new HashMap<>();
        validRequest.put("name", "Computer Science");
        validRequest.put("code", "CS");

        Map<String, String> invalidRequest = new HashMap<>();
        invalidRequest.put("name", "");
        invalidRequest.put("code", "CS");

        // Admin - Valid: Success
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CS"));

        // Admin - Invalid: Bad Request
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Student - Forbidden
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDepartmentValidatesInputAndEnforcesAdmin() throws Exception {
        Department d1 = new Department("Computer Science", "CS");
        d1.setId(10L);
        when(departmentService.update(10L, "Computer Science", "CS")).thenReturn(d1);

        Map<String, String> validRequest = new HashMap<>();
        validRequest.put("name", "Computer Science");
        validRequest.put("code", "CS");

        Map<String, String> invalidRequest = new HashMap<>();
        invalidRequest.put("name", "Computer Science");
        invalidRequest.put("code", " "); // spaces

        // Admin - Valid: Success
        mockMvc.perform(put("/api/departments/{id}", 10L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // Admin - Invalid: Bad Request
        mockMvc.perform(put("/api/departments/{id}", 10L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Student - Forbidden
        mockMvc.perform(put("/api/departments/{id}", 10L)
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDepartmentEnforcesAdmin() throws Exception {
        doNothing().when(departmentService).delete(10L);

        // Admin: Success
        mockMvc.perform(delete("/api/departments/{id}", 10L)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());

        // Student: Forbidden
        mockMvc.perform(delete("/api/departments/{id}", 10L)
                        .with(csrf())
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }
}
