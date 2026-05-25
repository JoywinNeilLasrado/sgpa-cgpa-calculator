package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.GradeUpdateRequest;
import com.gradecalculator.dto.EnrollmentResponse;
import com.gradecalculator.dto.request.AssignCourseRequest;
import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.*;
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

@WebMvcTest(controllers = FacultyGradeController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class FacultyGradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private FacultyService facultyService;
    @MockitoBean private CourseService courseService;
    @MockitoBean private SemesterService semesterService;
    @MockitoBean private EnrollmentService enrollmentService;
    @MockitoBean private UserService userService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal facultyPrincipal;

    private AppUser facultyUser;
    private AppUser anotherFacultyUser;

    @BeforeEach
    void setUp() {
        AppUser admin = new AppUser(1L, "admin", "pwd", AppUser.Role.ADMIN, "Admin", "admin@exam.com", "CS");
        adminPrincipal = new UserPrincipal(admin);

        facultyUser = new AppUser(2L, "faculty", "pwd", AppUser.Role.FACULTY, "Prof. Bob", "bob@exam.com", "CS");
        facultyPrincipal = new UserPrincipal(facultyUser);

        anotherFacultyUser = new AppUser(5L, "other", "pwd", AppUser.Role.FACULTY, "Prof. Alice", "alice@exam.com", "CS");

        reset(facultyService, courseService, semesterService, enrollmentService, userService);
    }

    @Test
    void getAllFacultyOnlyPermittedForAdmin() throws Exception {
        when(facultyService.findAllFacultyMembers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/faculty/members")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/faculty/members")
                        .with(user(facultyPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerFacultyOnlyPermittedForAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("faculty2");
        request.setPassword("Password123!");
        request.setName("Prof. Two");
        request.setEmail("two@exam.com");

        AppUser savedUser = new AppUser(10L, "faculty2", "pwd", AppUser.Role.FACULTY, "Prof. Two", "two@exam.com", null);
        when(userService.register(any(RegisterRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/faculty/register")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));

        mockMvc.perform(post("/api/faculty/register")
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFacultyOnlyPermittedForAdmin() throws Exception {
        doNothing().when(facultyService).deleteFacultyMember(10L);

        mockMvc.perform(delete("/api/faculty/members/{id}", 10L)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/faculty/members/{id}", 10L)
                        .with(csrf())
                        .with(user(facultyPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateFacultyOnlyPermittedForAdmin() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Bob Updated");

        when(facultyService.updateFacultyMember(eq(2L), eq("Bob Updated"), any(), any(), any())).thenReturn(facultyUser);

        mockMvc.perform(put("/api/faculty/members/{id}", 2L)
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/faculty/members/{id}", 2L)
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignFacultyToCourseOnlyPermittedForAdmin() throws Exception {
        AssignCourseRequest request = new AssignCourseRequest(20L, 2L);
        doNothing().when(facultyService).assignFacultyToCourse(20L, 2L);

        mockMvc.perform(post("/api/faculty/assign-course")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/faculty/assign-course")
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCoursesFiltersByFacultyRole() throws Exception {
        Course course = new Course("CS101", "Intro CS", 4);
        course.setId(20L);

        // Admin: Returns all
        when(courseService.findAll()).thenReturn(Collections.singletonList(course));
        mockMvc.perform(get("/api/faculty/courses")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());
        verify(courseService).findAll();

        // Faculty: Returns assigned only
        when(courseService.findByFacultyId(2L)).thenReturn(Collections.singletonList(course));
        mockMvc.perform(get("/api/faculty/courses")
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());
        verify(courseService).findByFacultyId(2L);
    }

    @Test
    void getEnrollmentsByCoursePermittedForAssignedFacultyAndAdmin() throws Exception {
        Course course = new Course("CS101", "Intro CS", 4);
        course.setId(20L);
        course.setFaculty(facultyUser); // bob assigned

        when(courseService.findById(20L)).thenReturn(Optional.of(course));
        when(enrollmentService.findByCourseId(20L)).thenReturn(Collections.emptyList());

        // Faculty Bob (assigned): Success
        mockMvc.perform(get("/api/faculty/enrollments/course/{courseId}", 20L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());

        // Faculty Alice (not assigned): Forbidden
        Course course2 = new Course("CS101", "Intro CS", 4);
        course2.setId(20L);
        course2.setFaculty(anotherFacultyUser); // alice assigned
        when(courseService.findById(20L)).thenReturn(Optional.of(course2));

        mockMvc.perform(get("/api/faculty/enrollments/course/{courseId}", 20L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isForbidden());

        // Admin: Success regardless
        mockMvc.perform(get("/api/faculty/enrollments/course/{courseId}", 20L)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getEnrollmentsBySemesterFiltersByFaculty() throws Exception {
        EnrollmentResponse r1 = new EnrollmentResponse(100L, 3L, "Stud", "R1", 20L, "CS101", "Intro", 4, "THEORY", 10L, 1, "O", 10, 40, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 0, 95);
        EnrollmentResponse r2 = new EnrollmentResponse(101L, 3L, "Stud", "R1", 21L, "CS102", "Data", 4, "THEORY", 10L, 1, "A+", 9, 36, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 0, 85);

        when(enrollmentService.findBySemesterId(10L)).thenReturn(Arrays.asList(r1, r2));

        Course c1 = new Course("CS101", "Intro", 4);
        c1.setFaculty(facultyUser); // Bob assigned to c1

        Course c2 = new Course("CS102", "Data", 4);
        c2.setFaculty(anotherFacultyUser); // Alice assigned to c2

        when(courseService.findById(20L)).thenReturn(Optional.of(c1));
        when(courseService.findById(21L)).thenReturn(Optional.of(c2));

        // Faculty Bob: returns only r1
        mockMvc.perform(get("/api/faculty/enrollments/semester/{semesterId}", 10L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100L));

        // Admin: returns both
        mockMvc.perform(get("/api/faculty/enrollments/semester/{semesterId}", 10L)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateGradePermittedForAssignedFacultyAndAdmin() throws Exception {
        Course course = new Course("CS101", "Intro CS", 4);
        course.setId(20L);
        course.setFaculty(facultyUser); // Bob assigned

        com.gradecalculator.model.Student stud = new com.gradecalculator.model.Student("Stud", "R1");
        Enrollment enrollment = new Enrollment(stud, course, null);
        enrollment.setId(100L);

        when(enrollmentService.findEnrollmentById(100L)).thenReturn(Optional.of(enrollment));
        
        Enrollment savedEnrollment = new Enrollment(stud, course, LetterGrade.A);
        savedEnrollment.setId(100L);
        when(enrollmentService.updateMarks(any(GradeUpdateRequest.class), anyString())).thenReturn(savedEnrollment);

        GradeUpdateRequest request = new GradeUpdateRequest(100L, LetterGrade.A, null, null, null, null, null, null, null, null, null, null, null, null);

        // Faculty Bob (assigned): Success
        mockMvc.perform(put("/api/faculty/grades")
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.grade").value("A"));

        // Faculty Bob (not assigned): Forbidden
        course.setFaculty(anotherFacultyUser); // Alice assigned now
        mockMvc.perform(put("/api/faculty/grades")
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Admin: Success (regardless of course assignment)
        mockMvc.perform(put("/api/faculty/grades")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void bulkUpdateGradesPermittedForAssignedFacultyAndAdmin() throws Exception {
        Course course = new Course("CS101", "Intro CS", 4);
        course.setId(20L);
        course.setFaculty(facultyUser); // Bob assigned

        com.gradecalculator.model.Student stud = new com.gradecalculator.model.Student("Stud", "R1");
        Enrollment enrollment = new Enrollment(stud, course, null);
        enrollment.setId(100L);

        when(enrollmentService.findEnrollmentById(100L)).thenReturn(Optional.of(enrollment));
        
        Enrollment savedEnrollment = new Enrollment(stud, course, LetterGrade.A);
        savedEnrollment.setId(100L);
        when(enrollmentService.updateMarks(any(GradeUpdateRequest.class), anyString())).thenReturn(savedEnrollment);

        List<GradeUpdateRequest> requests = Collections.singletonList(new GradeUpdateRequest(100L, LetterGrade.A, null, null, null, null, null, null, null, null, null, null, null, null));

        // Faculty Bob (assigned): Success
        mockMvc.perform(put("/api/faculty/grades/bulk")
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        // Faculty Bob (not assigned): Forbidden
        course.setFaculty(anotherFacultyUser); // Alice assigned now
        mockMvc.perform(put("/api/faculty/grades/bulk")
                        .with(csrf())
                        .with(user(facultyPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isForbidden());
    }
}
