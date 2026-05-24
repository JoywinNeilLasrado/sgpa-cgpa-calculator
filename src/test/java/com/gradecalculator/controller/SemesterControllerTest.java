package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.model.Semester;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.SemesterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradecalculator.config.SecurityConfig;
import com.gradecalculator.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = SemesterController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SemesterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private SemesterService semesterService;
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

        reset(semesterService);
    }

    @Test
    void getAllSemestersPermittedForAuthenticated() throws Exception {
        Semester s1 = new Semester(1);
        s1.setId(10L);
        when(semesterService.findAll()).thenReturn(Collections.singletonList(s1));

        mockMvc.perform(get("/api/semesters")
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].semesterNumber").value(1));
    }

    @Test
    void getSemesterByIdReturnsSemester() throws Exception {
        Semester s1 = new Semester(1);
        s1.setId(10L);
        when(semesterService.findById(10L)).thenReturn(Optional.of(s1));
        when(semesterService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/semesters/{id}", 10L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        mockMvc.perform(get("/api/semesters/{id}", 99L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSemesterValidatesInputAndEnforcesAdmin() throws Exception {
        Semester s1 = new Semester(1);
        s1.setId(10L);
        when(semesterService.create(1)).thenReturn(s1);

        Map<String, Integer> validRequest = new HashMap<>();
        validRequest.put("semesterNumber", 1);

        Map<String, Integer> invalidRequest = new HashMap<>();
        invalidRequest.put("semesterNumber", 15); // > 10

        // Admin - Valid: Success
        mockMvc.perform(post("/api/semesters")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterNumber").value(1));

        // Admin - Invalid: Bad Request (IllegalArgumentException)
        mockMvc.perform(post("/api/semesters")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Student - Forbidden
        mockMvc.perform(post("/api/semesters")
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSemesterValidatesInputAndEnforcesAdmin() throws Exception {
        Semester s1 = new Semester(2);
        s1.setId(10L);
        when(semesterService.update(10L, 2)).thenReturn(s1);

        Map<String, Integer> validRequest = new HashMap<>();
        validRequest.put("semesterNumber", 2);

        Map<String, Integer> invalidRequest = new HashMap<>();
        invalidRequest.put("semesterNumber", 0); // < 1

        // Admin - Valid: Success
        mockMvc.perform(put("/api/semesters/{id}", 10L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterNumber").value(2));

        // Admin - Invalid: Bad Request
        mockMvc.perform(put("/api/semesters/{id}", 10L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Student - Forbidden
        mockMvc.perform(put("/api/semesters/{id}", 10L)
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSemesterEnforcesAdmin() throws Exception {
        doNothing().when(semesterService).delete(10L);

        // Admin: Success
        mockMvc.perform(delete("/api/semesters/{id}", 10L)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());

        // Student: Forbidden
        mockMvc.perform(delete("/api/semesters/{id}", 10L)
                        .with(csrf())
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }
}
