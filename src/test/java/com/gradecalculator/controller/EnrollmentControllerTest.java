package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.EnrollmentResponse;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

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

@WebMvcTest(controllers = EnrollmentController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private EnrollmentService enrollmentService;
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

        reset(enrollmentService, securityExpressionEvaluator);
    }

    @Test
    void getAllEnrollmentsPermittedForAdminAndFaculty() throws Exception {
        EnrollmentResponse r1 = new EnrollmentResponse(100L, 3L, "Student Name", "CS2024", 20L, "CS101", "Intro CS", 4, "THEORY", 10L, 1, "O", 10, 40, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 0, 95);
        when(enrollmentService.findAll()).thenReturn(Arrays.asList(r1));

        // Admin: Success
        mockMvc.perform(get("/api/enrollments")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        // Faculty: Success
        mockMvc.perform(get("/api/enrollments")
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());

        // Student: Forbidden
        mockMvc.perform(get("/api/enrollments")
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEnrollmentsByStudentPermittedForOwnerAndStaff() throws Exception {
        EnrollmentResponse r1 = new EnrollmentResponse(100L, 3L, "Student Name", "CS2024", 20L, "CS101", "Intro CS", 4, "THEORY", 10L, 1, "O", 10, 40, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 0, 95);
        when(enrollmentService.findByStudentId(3L)).thenReturn(Collections.singletonList(r1));

        // Owner: Success
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(true);
        mockMvc.perform(get("/api/enrollments/student/{studentId}", 3L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        // Non-Owner Student: Forbidden
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(false);
        mockMvc.perform(get("/api/enrollments/student/{studentId}", 3L)
                        .with(user(anotherStudentPrincipal)))
                .andExpect(status().isForbidden());

        // Admin: Success
        mockMvc.perform(get("/api/enrollments/student/{studentId}", 3L)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());

        // Faculty: Success
        mockMvc.perform(get("/api/enrollments/student/{studentId}", 3L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getEnrollmentsByStudentAndSemesterPermittedForOwnerAndStaff() throws Exception {
        EnrollmentResponse r1 = new EnrollmentResponse(100L, 3L, "Student Name", "CS2024", 20L, "CS101", "Intro CS", 4, "THEORY", 10L, 1, "O", 10, 40, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 0, 95);
        when(enrollmentService.findByStudentIdAndSemesterId(3L, 10L)).thenReturn(Collections.singletonList(r1));

        // Owner: Success
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(true);
        mockMvc.perform(get("/api/enrollments/student/{studentId}/semester/{semesterId}", 3L, 10L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        // Non-Owner Student: Forbidden
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(false);
        mockMvc.perform(get("/api/enrollments/student/{studentId}/semester/{semesterId}", 3L, 10L)
                        .with(user(anotherStudentPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createEnrollmentEnforcesOwnership() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(3L);
        request.setCourseId(20L);
        request.setCieMarks(48);
        request.setSeeMarks(47);

        EnrollmentResponse r1 = new EnrollmentResponse(100L, 3L, "Student Name", "CS2024", 20L, "CS101", "Intro CS", 4, "THEORY", 10L, 1, "O", 10, 40, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 0, 95);
        when(enrollmentService.create(any(EnrollmentRequest.class))).thenReturn(r1);

        // Owner Student: Success
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(3L))).thenReturn(true);
        mockMvc.perform(post("/api/enrollments")
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        // Non-Owner Student: Forbidden
        request.setStudentId(4L); // trying to enroll for student 4
        when(securityExpressionEvaluator.isStudentOwner(any(), eq(4L))).thenReturn(false);
        mockMvc.perform(post("/api/enrollments")
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Admin: Success (regardless of isStudentOwner evaluation since role-based expression allows ADMIN)
        mockMvc.perform(post("/api/enrollments")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateEnrollmentOnlyPermittedForAdmin() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(3L);
        request.setCourseId(20L);
        request.setCieMarks(42);
        request.setSeeMarks(43);

        EnrollmentResponse r1 = new EnrollmentResponse(100L, 3L, "Student Name", "CS2024", 20L, "CS101", "Intro CS", 4, "THEORY", 10L, 1, "A+", 9, 36, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 0, 85);
        when(enrollmentService.update(eq(100L), any(EnrollmentRequest.class))).thenReturn(r1);

        // Admin: Success
        mockMvc.perform(put("/api/enrollments/{id}", 100L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("A+"));

        // Faculty: Forbidden
        mockMvc.perform(put("/api/enrollments/{id}", 100L)
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Student: Forbidden
        mockMvc.perform(put("/api/enrollments/{id}", 100L)
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEnrollmentPermittedForAssignedFaculty() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(3L);
        request.setCourseId(20L);
        request.setCieMarks(42);
        request.setSeeMarks(43);

        EnrollmentResponse r1 = new EnrollmentResponse(100L, 3L, "Student Name", "CS2024", 20L, "CS101", "Intro CS", 4, "THEORY", 10L, 1, "A+", 9, 36, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 0, 85);
        when(enrollmentService.update(eq(100L), any(EnrollmentRequest.class))).thenReturn(r1);
        when(securityExpressionEvaluator.isAuthorizedToModifyEnrollment(any(), eq(100L))).thenReturn(true);

        mockMvc.perform(put("/api/enrollments/{id}", 100L)
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("A+"));
    }

    @Test
    void updateEnrollmentForbiddenForUnassignedFaculty() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(3L);
        request.setCourseId(20L);
        request.setCieMarks(42);
        request.setSeeMarks(43);

        when(securityExpressionEvaluator.isAuthorizedToModifyEnrollment(any(), eq(100L))).thenReturn(false);

        mockMvc.perform(put("/api/enrollments/{id}", 100L)
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEnrollmentOnlyPermittedForAdmin() throws Exception {
        doNothing().when(enrollmentService).delete(100L);

        // Admin: Success
        mockMvc.perform(delete("/api/enrollments/{id}", 100L)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());

        // Faculty: Forbidden
        mockMvc.perform(delete("/api/enrollments/{id}", 100L)
                        .with(csrf())
                        .with(user(facultyPrincipal)))
                .andExpect(status().isForbidden());
    }
}
