package com.gradecalculator.integration;

import com.gradecalculator.dto.SgpaResponse;
import com.gradecalculator.model.*;
import com.gradecalculator.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for SGPA and CGPA calculation endpoints.
 */
@SpringBootTest(properties = {
        "jwt.secret=MySuperSecretKey1234567890123456MySuperSecretKey1234567890123456"
})
@AutoConfigureMockMvc
class GradeCalculationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.gradecalculator.repository.LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private com.gradecalculator.security.LoginRateLimiterService rateLimiter;

    private String adminToken;
    private Long studentId;
    private Long semester1Id;
    private Long semester2Id;
    private Long course1Id;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        semesterRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();

        // Seed the admin user with password 'admin123'
        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setName("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(AppUser.Role.ADMIN);
        userRepository.save(admin);

        // Clear rate limiting block on 127.0.0.1 and admin user
        loginAttemptRepository.deleteAll();
        rateLimiter.loginSucceeded("127.0.0.1");
        rateLimiter.accountLoginSucceeded("admin");

        adminToken = getAdminToken();

        // Create semesters
        Semester sem1 = semesterRepository.save(new Semester(1));
        Semester sem2 = semesterRepository.save(new Semester(2));
        semester1Id = sem1.getId();
        semester2Id = sem2.getId();

        // Create a student
        String studentRequest = """
            {
                "name": "Grade Test Student",
                "studentId": "GR2024001",
                "branch": "Computer Science",
                "dateOfBirth": "2004-01-01"
            }
            """;

        MvcResult studentResult = mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentRequest))
                .andExpect(status().isOk())
                .andReturn();

        studentId = objectMapper.readTree(studentResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Create a course
        String courseRequest = String.format("""
            {
                "courseCode": "CS101",
                "courseName": "Introduction to Programming",
                "credits": 4,
                "courseType": "THEORY",
                "semesterId": %d
            }
            """, semester1Id);

        MvcResult courseResult = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseRequest))
                .andExpect(status().isOk())
                .andReturn();

        course1Id = objectMapper.readTree(courseResult.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private String getAdminToken() throws Exception {
        String requestBody = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SGPA Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/sgpa/student/{id}/semester/{semId} - should calculate SGPA")
    void calculateSGPA() throws Exception {
        // Create enrollment
        createEnrollment(studentId, course1Id, "A");

        mockMvc.perform(get("/api/sgpa/student/" + studentId + "/semester/" + semester1Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.semesterId").value(semester1Id))
                .andExpect(jsonPath("$.sgpa").isNumber());
    }

    @Test
    @DisplayName("GET /api/sgpa/student/{id}/semester/{semId} - should return 0 for no enrollments")
    void calculateSGPAWithNoEnrollments() throws Exception {
        mockMvc.perform(get("/api/sgpa/student/" + studentId + "/semester/" + semester2Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("No enrollments")));
    }

    @Test
    @DisplayName("GET /api/sgpa/student/{id}/semester/{semId} - should return 404 for non-existent student")
    void calculateSGPAWithInvalidStudent() throws Exception {
        mockMvc.perform(get("/api/sgpa/student/99999/semester/" + semester1Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("STUDENT_NOT_FOUND"));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // CGPA Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/cgpa/student/{id} - should calculate overall CGPA")
    void calculateOverallCGPA() throws Exception {
        // Create enrollment
        createEnrollment(studentId, course1Id, "A+");

        mockMvc.perform(get("/api/cgpa/student/" + studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.cgpa").isNumber());
    }

    @Test
    @DisplayName("GET /api/cgpa/student/{id} - should return 0 for no enrollments")
    void calculateOverallCGPAWithNoEnrollments() throws Exception {
        mockMvc.perform(get("/api/cgpa/student/" + studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cgpa").value(0.0))
                .andExpect(jsonPath("$.totalEarnedCredits").value(0));
    }

    @Test
    @DisplayName("GET /api/cgpa/student/{id}/semester/{semId} - should calculate CGPA up to semester")
    void calculateCGPAUpToSemester() throws Exception {
        // Create enrollment in semester 1
        createEnrollment(studentId, course1Id, "A");

        mockMvc.perform(get("/api/cgpa/student/" + studentId + "/semester/" + semester1Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cgpa").isNumber());
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Grade Scale Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/grade-scale - should return grading scale")
    void getGradingScale() throws Exception {
        mockMvc.perform(get("/api/grade-scale")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grades").isMap())
                .andExpect(jsonPath("$.grades.O.performance").value("Outstanding"))
                .andExpect(jsonPath("$.grades.O.points").value(10));
    }

    @Test
    @DisplayName("GET /api/grades/from-marks - should return grade for marks")
    void getGradeFromMarks() throws Exception {
        mockMvc.perform(get("/api/grades/from-marks")
                        .param("marks", "85")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marks").value(85))
                .andExpect(jsonPath("$.grade").value("A+"))
                .andExpect(jsonPath("$.performance").value("Excellent"))
                .andExpect(jsonPath("$.points").value(9));
    }

    @Test
    @DisplayName("GET /api/grades/from-marks - should return F for low marks")
    void getGradeFromLowMarks() throws Exception {
        mockMvc.perform(get("/api/grades/from-marks")
                        .param("marks", "30")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marks").value(30))
                .andExpect(jsonPath("$.grade").value("F"))
                .andExpect(jsonPath("$.performance").value("Fail"))
                .andExpect(jsonPath("$.points").value(0));
    }

    @Test
    @DisplayName("GET /api/grades/from-marks - should return O for high marks")
    void getGradeFromHighMarks() throws Exception {
        mockMvc.perform(get("/api/grades/from-marks")
                        .param("marks", "95")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marks").value(95))
                .andExpect(jsonPath("$.grade").value("O"))
                .andExpect(jsonPath("$.performance").value("Outstanding"))
                .andExpect(jsonPath("$.points").value(10));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════════════════

    private Long createEnrollment(Long studentId, Long courseId, String grade) throws Exception {
        String requestBody = String.format("""
            {
                "studentId": %d,
                "courseId": %d,
                "grade": "%s"
            }
            """, studentId, courseId, grade);

        MvcResult result = mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
