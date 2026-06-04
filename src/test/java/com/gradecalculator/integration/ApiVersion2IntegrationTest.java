package com.gradecalculator.integration;

import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.repository.StudentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.model.Semester;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

/**
 * Integration tests for API v2 endpoints and exception handling.
 */
@SpringBootTest(properties = {
        "jwt.secret=MySuperSecretKey1234567890123456MySuperSecretKey1234567890123456"
})
@AutoConfigureMockMvc
class ApiVersion2IntegrationTest {

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

    @BeforeEach
    void setUp() throws Exception {
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

        // Create test semesters
        if (semesterRepository.count() == 0) {
            semesterRepository.save(new Semester(1));
            semesterRepository.save(new Semester(2));
        }
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
    // API v2 Student Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/v2/students - should return paginated list")
    void getPaginatedStudents() throws Exception {
        // Create some students first
        createStudent("Alice", "CS2024001");
        createStudent("Bob", "CS2024002");
        createStudent("Charlie", "CS2024003");

        mockMvc.perform(get("/api/v2/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v2/students - should return empty page for no results")
    void getEmptyPaginatedStudents() throws Exception {
        mockMvc.perform(get("/api/v2/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v2/students - should create student with ApiResponse wrapper")
    void createStudentV2() throws Exception {
        String requestBody = """
            {
                "name": "New Student",
                "studentId": "CS2024099",
                "branch": "Computer Science",
                "dateOfBirth": "2004-01-01"
            }
            """;

        mockMvc.perform(post("/api/v2/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("created")))
                .andExpect(jsonPath("$.data.name").value("New Student"));
    }

    @Test
    @DisplayName("GET /api/v2/students/{id} - should return student by ID")
    void getStudentByIdV2() throws Exception {
        Long studentId = createStudent("Test", "CS2024098");

        mockMvc.perform(get("/api/v2/students/" + studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test"));
    }

    @Test
    @DisplayName("GET /api/v2/students/{id} - should return 404 with error code")
    void getNonExistentStudentV2() throws Exception {
        mockMvc.perform(get("/api/v2/students/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v2/students/{id} - should update student")
    void updateStudentV2() throws Exception {
        Long studentId = createStudent("Original", "CS2024097");

        String updateBody = """
            {
                "name": "Updated",
                "studentId": "CS2024097",
                "branch": "IT",
                "dateOfBirth": "2004-02-02"
            }
            """;

        mockMvc.perform(put("/api/v2/students/" + studentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("updated")))
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    @DisplayName("DELETE /api/v2/students/{id} - should delete student")
    void deleteStudentV2() throws Exception {
        Long studentId = createStudent("ToDelete", "CS2024096");

        mockMvc.perform(delete("/api/v2/students/" + studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("deleted")));
    }

    @Test
    @DisplayName("GET /api/v2/students/search - should search by username")
    void searchStudentV2() throws Exception {
        createStudent("Searchable", "CS2024095");

        mockMvc.perform(get("/api/v2/students/search")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("username", "CS2024095"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Searchable"));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Exception Handler Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle StudentNotFoundException with STUDENT_NOT_FOUND error code")
    void handleStudentNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v2/students/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("STUDENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should handle ValidationException for invalid input")
    void handleValidationException() throws Exception {
        String invalidRequest = """
            {
                "name": "",
                "studentId": ""
            }
            """;

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Should return proper JSON structure for all error responses")
    void properErrorResponseStructure() throws Exception {
        mockMvc.perform(get("/api/v2/students/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    // Helper methods
    private Long createStudent(String name, String rollNumber) throws Exception {
        String requestBody = String.format("""
            {
                "name": "%s",
                "studentId": "%s",
                "branch": "Computer Science",
                "dateOfBirth": "2004-01-01"
            }
            """, name, rollNumber);

        MvcResult result = mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
