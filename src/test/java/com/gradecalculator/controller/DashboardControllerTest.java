package com.gradecalculator.controller;

import com.gradecalculator.dto.DashboardResponse;
import com.gradecalculator.dto.SemesterResultRowResponse;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradecalculator.config.SecurityConfig;
import com.gradecalculator.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = DashboardController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private DashboardService dashboardService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal facultyPrincipal;
    private UserPrincipal studentPrincipal;
    private UserPrincipal anotherStudentPrincipal;

    @BeforeEach
    void setUp() {
        com.gradecalculator.model.AppUser admin = new com.gradecalculator.model.AppUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(com.gradecalculator.model.AppUser.Role.ADMIN);
        adminPrincipal = new UserPrincipal(admin);

        com.gradecalculator.model.AppUser faculty = new com.gradecalculator.model.AppUser();
        faculty.setId(2L);
        faculty.setUsername("faculty");
        faculty.setRole(com.gradecalculator.model.AppUser.Role.FACULTY);
        facultyPrincipal = new UserPrincipal(faculty);

        com.gradecalculator.model.AppUser student = new com.gradecalculator.model.AppUser();
        student.setId(3L);
        student.setUsername("student");
        student.setRole(com.gradecalculator.model.AppUser.Role.STUDENT);
        studentPrincipal = new UserPrincipal(student);

        com.gradecalculator.model.AppUser anotherStudent = new com.gradecalculator.model.AppUser();
        anotherStudent.setId(4L);
        anotherStudent.setUsername("anotherStudent");
        anotherStudent.setRole(com.gradecalculator.model.AppUser.Role.STUDENT);
        anotherStudentPrincipal = new UserPrincipal(anotherStudent);

        reset(dashboardService, securityExpressionEvaluator);
    }

    @Test
    void getStudentDashboardPermittedForOwnerAndStaff() throws Exception {
        DashboardResponse response = new DashboardResponse();
        response.setStudentId(3L);
        response.setStudentName("Student Name");
        response.setStudentRoll("CS2024");
        response.setCgpa(9.0);
        when(dashboardService.getStudentDashboard(3L)).thenReturn(response);

        // Owner: Success
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(true);
        mockMvc.perform(get("/api/students/{id}/dashboard", 3L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Student Name"))
                .andExpect(jsonPath("$.cgpa").value(9.0));

        // Non-Owner Student: Forbidden
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(false);
        mockMvc.perform(get("/api/students/{id}/dashboard", 3L)
                        .with(user(anotherStudentPrincipal)))
                .andExpect(status().isForbidden());

        // Admin: Success
        mockMvc.perform(get("/api/students/{id}/dashboard", 3L)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());

        // Faculty: Success
        mockMvc.perform(get("/api/students/{id}/dashboard", 3L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getSemesterResultPermittedForOwnerAndStaff() throws Exception {
        SemesterResultRowResponse row = new SemesterResultRowResponse("CS101", "Intro CS", 4, "O", 10, 40);
        when(dashboardService.getSemesterResult(3L, 10L)).thenReturn(Collections.singletonList(row));

        // Owner: Success
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(true);
        mockMvc.perform(get("/api/results/student/{studentId}/semester/{semesterId}", 3L, 10L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"))
                .andExpect(jsonPath("$[0].grade").value("O"));

        // Non-Owner Student: Forbidden
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(false);
        mockMvc.perform(get("/api/results/student/{studentId}/semester/{semesterId}", 3L, 10L)
                        .with(user(anotherStudentPrincipal)))
                .andExpect(status().isForbidden());
    }
}
