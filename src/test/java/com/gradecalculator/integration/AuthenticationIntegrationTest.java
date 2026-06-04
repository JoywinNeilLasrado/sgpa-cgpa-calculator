package com.gradecalculator.integration;

import com.gradecalculator.model.AppUser;
import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints.
 */
@SpringBootTest(properties = {
        "jwt.secret=MySuperSecretKey1234567890123456MySuperSecretKey1234567890123456"
})
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    private com.gradecalculator.security.LoginRateLimiterService rateLimiter;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.gradecalculator.repository.LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
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
    }

    private String getAdminToken() throws Exception {
        String requestBody = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResponse).get("token").asText();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDownRateLimiter() {
        rateLimiter.loginSucceeded("127.0.0.1");
        rateLimiter.accountLoginSucceeded("admin");
    }

    @Test
    @DisplayName("POST /api/auth/login - should return JWT token for valid credentials")
    void loginWithValidCredentials() throws Exception {
        String requestBody = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 401 for invalid credentials")
    void loginWithInvalidCredentials() throws Exception {
        String requestBody = """
            {
                "username": "admin",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Invalid")));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 400 for missing username")
    void loginWithMissingUsername() throws Exception {
        String requestBody = """
            {
                "password": "admin123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 400 for empty request body")
    void loginWithEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register - should create new user and return tokens")
    void registerWithValidData() throws Exception {
        String requestBody = """
            {
                "username": "NEWUSER",
                "password": "SecurePass123!",
                "role": "STUDENT",
                "name": "New User"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("NEWUSER"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 409 for duplicate username")
    void registerWithDuplicateUsername() throws Exception {
        // Register first user
        String requestBody = """
            {
                "username": "DUPLICATE",
                "password": "SecurePass123!",
                "role": "STUDENT",
                "name": "First User"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // Try to register second user with same username
        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 for invalid role")
    void registerWithInvalidRole() throws Exception {
        String requestBody = """
            {
                "username": "NEWUSER",
                "password": "SecurePass123!",
                "role": "INVALID_ROLE",
                "name": "New User"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - should handle rate limiting after multiple failures")
    void loginWithRateLimiting() throws Exception {
        String invalidRequest = """
            {
                "username": "nonexistent",
                "password": "wrongpassword"
            }
            """;

        // Make 5 failed login attempts (rate limit threshold)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest));
        }

        // 6th attempt should be rate limited
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("too many")));
    }
}
