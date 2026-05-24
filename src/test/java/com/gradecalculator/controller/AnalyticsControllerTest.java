package com.gradecalculator.controller;

import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.AnalyticsService;
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

@WebMvcTest(controllers = AnalyticsController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AnalyticsService analyticsService;
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

        reset(analyticsService, securityExpressionEvaluator);
    }

    @Test
    void getSGPATrendsPermittedForOwnerAndStaff() throws Exception {
        Map<String, Object> trend = new HashMap<>();
        trend.put("semester", "Sem 1");
        trend.put("sgpa", 8.5);
        when(analyticsService.getSGPATrends(3L)).thenReturn(Collections.singletonList(trend));

        // Owner: Success
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(true);
        mockMvc.perform(get("/api/analytics/sgpa-trends/{studentId}", 3L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].semester").value("Sem 1"))
                .andExpect(jsonPath("$[0].sgpa").value(8.5));

        // Non-Owner Student: Forbidden
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(false);
        mockMvc.perform(get("/api/analytics/sgpa-trends/{studentId}", 3L)
                        .with(user(anotherStudentPrincipal)))
                .andExpect(status().isForbidden());

        // Admin: Success
        mockMvc.perform(get("/api/analytics/sgpa-trends/{studentId}", 3L)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());

        // Faculty: Success
        mockMvc.perform(get("/api/analytics/sgpa-trends/{studentId}", 3L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getRankingsPermittedForStaff() throws Exception {
        Map<String, Object> ranking = new HashMap<>();
        ranking.put("name", "Bob");
        ranking.put("cgpa", 9.5);
        when(analyticsService.getRankings(anyInt())).thenReturn(Collections.singletonList(ranking));

        // Admin: Success
        mockMvc.perform(get("/api/analytics/rankings")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bob"));

        // Faculty: Success
        mockMvc.perform(get("/api/analytics/rankings")
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());

        // Student: Forbidden
        mockMvc.perform(get("/api/analytics/rankings")
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getToppersPermittedForStaff() throws Exception {
        Map<String, Object> topper = new HashMap<>();
        topper.put("name", "Bob");
        when(analyticsService.getToppers(anyInt())).thenReturn(Collections.singletonList(topper));

        // Admin: Success
        mockMvc.perform(get("/api/analytics/toppers")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bob"));

        // Student: Forbidden
        mockMvc.perform(get("/api/analytics/toppers")
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCourseAnalyticsPermittedForStaff() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("averageGrade", 8.2);
        when(analyticsService.getCourseAnalytics(20L)).thenReturn(response);

        // Faculty: Success
        mockMvc.perform(get("/api/analytics/course/{courseId}", 20L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageGrade").value(8.2));

        // Student: Forbidden
        mockMvc.perform(get("/api/analytics/course/{courseId}", 20L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getClassStatisticsPermittedForStaff() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("totalStudents", 50);
        when(analyticsService.getClassStatistics()).thenReturn(response);

        // Faculty: Success
        mockMvc.perform(get("/api/analytics/class")
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(50));

        // Student: Forbidden
        mockMvc.perform(get("/api/analytics/class")
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }
}
