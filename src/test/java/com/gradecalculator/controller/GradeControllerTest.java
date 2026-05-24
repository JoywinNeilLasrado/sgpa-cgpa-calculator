package com.gradecalculator.controller;

import com.gradecalculator.dto.CgpaResponse;
import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.GradeCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradecalculator.config.SecurityConfig;
import com.gradecalculator.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = GradeController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private GradeCalculationService gradeCalculationService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        com.gradecalculator.model.AppUser admin = new com.gradecalculator.model.AppUser();
        admin.setUsername("admin");
        admin.setRole(com.gradecalculator.model.AppUser.Role.ADMIN);
        adminPrincipal = new UserPrincipal(admin);
    }

    @Test
    void calculateSGPASucceedsForAuthorizedUser() throws Exception {
        Long studentId = 1L;
        Long semesterId = 10L;
        
        SgpaResponse response = new SgpaResponse(studentId, semesterId, 8.5, 20, 170);
        when(gradeCalculationService.calculateSGPA(studentId, semesterId)).thenReturn(response);
        when(securityExpressionEvaluator.isStudentOwner(any(UserPrincipal.class), eq(studentId))).thenReturn(true);

        mockMvc.perform(get("/api/sgpa/student/{studentId}/semester/{semesterId}", studentId, semesterId)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sgpa").value(8.5))
                .andExpect(jsonPath("$.totalCredits").value(20))
                .andExpect(jsonPath("$.totalCreditPoints").value(170));
    }

    @Test
    void calculateSGPADeniedForUnauthorizedUser() throws Exception {
        Long studentId = 1L;
        Long semesterId = 10L;

        com.gradecalculator.model.AppUser student = new com.gradecalculator.model.AppUser();
        student.setUsername("otherStudent");
        student.setRole(com.gradecalculator.model.AppUser.Role.STUDENT);
        UserPrincipal studentPrincipal = new UserPrincipal(student);

        when(securityExpressionEvaluator.isStudentOwner(any(UserPrincipal.class), eq(studentId))).thenReturn(false);

        mockMvc.perform(get("/api/sgpa/student/{studentId}/semester/{semesterId}", studentId, semesterId)
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void calculateCGPASucceedsForAuthorizedUser() throws Exception {
        Long studentId = 1L;
        Long semesterId = 10L;

        CgpaResponse response = new CgpaResponse(studentId, 9.0, 40, 360, 2);
        when(gradeCalculationService.calculateCGPA(studentId, semesterId)).thenReturn(response);
        when(securityExpressionEvaluator.isStudentOwner(any(UserPrincipal.class), eq(studentId))).thenReturn(true);

        mockMvc.perform(get("/api/cgpa/student/{studentId}/semester/{semesterId}", studentId, semesterId)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cgpa").value(9.0))
                .andExpect(jsonPath("$.totalEarnedCredits").value(40));
    }

    @Test
    void calculateOverallCGPASucceedsForAuthorizedUser() throws Exception {
        Long studentId = 1L;

        CgpaResponse response = new CgpaResponse(studentId, 9.2, 80, 736, 4);
        when(gradeCalculationService.calculateOverallCGPA(studentId)).thenReturn(response);
        when(securityExpressionEvaluator.isStudentOwner(any(UserPrincipal.class), eq(studentId))).thenReturn(true);

        mockMvc.perform(get("/api/cgpa/student/{studentId}", studentId)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cgpa").value(9.2))
                .andExpect(jsonPath("$.semestersCompleted").value(4));
    }

    @Test
    void getGradeScaleSucceedsPublicly() throws Exception {
        mockMvc.perform(get("/api/grade-scale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grades.O.performance").value("Outstanding"))
                .andExpect(jsonPath("$.grades.O.points").value(10))
                .andExpect(jsonPath("$.grades.F.performance").value("Fail"));
    }

    @Test
    void getGradeFromMarksSucceedsPublicly() throws Exception {
        mockMvc.perform(get("/api/grades/from-marks")
                        .param("marks", "85"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marks").value(85))
                .andExpect(jsonPath("$.grade").value("A+"))
                .andExpect(jsonPath("$.performance").value("Excellent"))
                .andExpect(jsonPath("$.points").value(9));
    }
}
