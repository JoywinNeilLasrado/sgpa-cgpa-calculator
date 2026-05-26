package com.gradecalculator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.request.LoginRequest;
import com.gradecalculator.dto.StudentRequest;
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
 * End-to-end integration test asserting Student lifecycle API endpoints
 * under real Spring context configurations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "app.demo.password=adminPassword123",
    "app.demo.seed=true",
    "jwt.secret=MySuperSecretKey1234567890123456MySuperSecretKey1234567890123456"
})
@AutoConfigureMockMvc
@Transactional
public class StudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testStudentLifecycleIntegration() throws Exception {
        // 1. Perform login to acquire JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("adminPassword123");

        String loginResponseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String jwtToken = objectMapper.readTree(loginResponseJson).get("token").asText();
        String authHeader = "Bearer " + jwtToken;

        // 2. Create student
        StudentRequest studentRequest = new StudentRequest();
        studentRequest.setName("Integration Student");
        studentRequest.setStudentId("INT-2026-001");
        studentRequest.setBranch("Computer Science");
        studentRequest.setDateOfBirth("2005-05-15");

        String studentJson = mockMvc.perform(post("/api/students")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Student"))
                .andExpect(jsonPath("$.studentId").value("INT-2026-001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long studentId = objectMapper.readTree(studentJson).get("id").asLong();

        // 3. Get Student
        mockMvc.perform(get("/api/students/" + studentId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Student"));

        // 4. Update Student
        studentRequest.setName("Integration Student Updated");
        mockMvc.perform(put("/api/students/" + studentId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Student Updated"));

        // 5. Delete Student
        mockMvc.perform(delete("/api/students/" + studentId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());
    }
}
