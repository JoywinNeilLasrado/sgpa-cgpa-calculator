package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.CourseRequest;
import com.gradecalculator.model.Course;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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

@WebMvcTest(controllers = CourseController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CourseService courseService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal studentPrincipal;

    @BeforeEach
    void setUp() {
        com.gradecalculator.model.AppUser admin = new com.gradecalculator.model.AppUser();
        admin.setUsername("admin");
        admin.setRole(com.gradecalculator.model.AppUser.Role.ADMIN);
        adminPrincipal = new UserPrincipal(admin);

        com.gradecalculator.model.AppUser student = new com.gradecalculator.model.AppUser();
        student.setUsername("student");
        student.setRole(com.gradecalculator.model.AppUser.Role.STUDENT);
        studentPrincipal = new UserPrincipal(student);
    }

    @Test
    void getAllCoursesReturnsListForAuthenticatedUser() throws Exception {
        Course c1 = new Course("CS101", "Programming", 4);
        when(courseService.findAll()).thenReturn(Arrays.asList(c1));

        mockMvc.perform(get("/api/courses")
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void getCoursesBySemesterReturnsList() throws Exception {
        Course c1 = new Course("CS101", "Programming", 4);
        when(courseService.findBySemesterId(10L)).thenReturn(Collections.singletonList(c1));

        mockMvc.perform(get("/api/courses/semester/{semesterId}", 10L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void getCourseReturnsCourseOptional() throws Exception {
        Course c1 = new Course("CS101", "Programming", 4);
        c1.setId(5L);
        when(courseService.findById(5L)).thenReturn(Optional.of(c1));

        mockMvc.perform(get("/api/courses/{id}", 5L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.courseCode").value("CS101"));
    }

    @Test
    void createCourseSucceedsForAdmin() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setCourseCode("CS102");
        request.setCourseName("Data Structures");
        request.setCredits(4);
        request.setSemesterId(10L);

        Course created = new Course("CS102", "Data Structures", 4);
        created.setId(20L);

        when(courseService.create(eq("CS102"), eq("Data Structures"), eq(4), eq(10L), any(), any())).thenReturn(created);

        mockMvc.perform(post("/api/courses")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20L))
                .andExpect(jsonPath("$.courseCode").value("CS102"));
    }

    @Test
    void createCourseForbiddenForStudent() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setCourseCode("CS102");
        request.setCourseName("Data Structures");
        request.setCredits(4);
        request.setSemesterId(10L);

        mockMvc.perform(post("/api/courses")
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCourseSucceedsForAdmin() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setCourseCode("CS102");
        request.setCourseName("Data Structures Edited");
        request.setCredits(4);
        request.setSemesterId(10L);

        Course updated = new Course("CS102", "Data Structures Edited", 4);
        updated.setId(20L);

        when(courseService.update(eq(20L), eq("CS102"), eq("Data Structures Edited"), eq(4), eq(10L), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/courses/{id}", 20L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Data Structures Edited"));
    }

    @Test
    void deleteCourseSucceedsForAdmin() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", 20L)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());

        verify(courseService).delete(20L);
    }
}
