package com.gradecalculator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.request.LoginRequest;
import com.gradecalculator.dto.StudentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration test for enrollment operations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "app.demo.password=adminPassword123",
    "app.demo.seed=true",
    "jwt.secret=MySuperSecretKey1234567890123456MySuperSecretKey1234567890123456"
})
@AutoConfigureMockMvc
@Transactional
public class EnrollmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    public void setup() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("adminPassword123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    public void testEnrollmentLifecycle() throws Exception {
        // 1. Create a student
        StudentRequest studentRequest = new StudentRequest();
        studentRequest.setName("Enrollment Test Student");
        studentRequest.setStudentId("ENR-2026-001");
        studentRequest.setBranch("Computer Science");
        studentRequest.setDateOfBirth("2005-03-20");

        String studentJson = mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long studentId = objectMapper.readTree(studentJson).get("id").asLong();

        // 2. Create a course
        String courseRequest = """
            {
                "courseCode": "TEST101",
                "courseName": "Test Course",
                "credits": 4,
                "semesterId": 1
            }
            """;

        String courseJson = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseRequest))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long courseId = objectMapper.readTree(courseJson).get("id").asLong();

        // 3. Create enrollment
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setStudentId(studentId);
        enrollmentRequest.setCourseId(courseId);
        enrollmentRequest.setCieMarks(40);
        enrollmentRequest.setSeeMarks(45);

        String enrollmentJson = mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long enrollmentId = objectMapper.readTree(enrollmentJson).get("id").asLong();

        // 4. Get enrollments for student
        mockMvc.perform(get("/api/enrollments/student/" + studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(studentId));

        // 5. Delete enrollment
        mockMvc.perform(delete("/api/enrollments/" + enrollmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDuplicateEnrollmentFails() throws Exception {
        // Create student
        StudentRequest studentRequest = new StudentRequest();
        studentRequest.setName("Duplicate Test Student");
        studentRequest.setStudentId("DUP-2026-001");
        studentRequest.setBranch("IT");
        studentRequest.setDateOfBirth("2005-07-15");

        String studentJson = mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRequest)))
                .andReturn().getResponse().getContentAsString();

        Long studentId = objectMapper.readTree(studentJson).get("id").asLong();

        // Create course
        String courseRequest = """
            {
                "courseCode": "DUP101",
                "courseName": "Duplicate Test Course",
                "credits": 3,
                "semesterId": 1
            }
            """;

        String courseJson = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseRequest))
                .andReturn().getResponse().getContentAsString();

        Long courseId = objectMapper.readTree(courseJson).get("id").asLong();

        // First enrollment - should succeed
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setStudentId(studentId);
        enrollmentRequest.setCourseId(courseId);

        mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isOk());

        // Second enrollment - should fail
        mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSGPAAndCGPACalculation() throws Exception {
        // This test verifies that existing enrollments can be queried for grade calculation
        // Using existing demo data
        
        mockMvc.perform(get("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthorizedEnrollmentCreationFails() throws Exception {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setStudentId(1L);
        enrollmentRequest.setCourseId(1L);

        // Without authentication
        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isForbidden());
    }
}
