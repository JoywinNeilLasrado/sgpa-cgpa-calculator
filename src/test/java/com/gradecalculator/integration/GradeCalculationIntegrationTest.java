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
@SpringBootTest
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
                .andExpect(status().isCreated())
                .andReturn();

        studentId = objectMapper.readTree(studentResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

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
                .andExpect(status().isCreated())
                .andReturn();

        course1Id = objectMapper.readTree(courseResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();
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
                .get("data").get("accessToken").asText();
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.semesterId").value(semester1Id))
                .andExpect(jsonPath("$.data.sgpa").isNumber());
    }

    @Test
    @DisplayName("GET /api/sgpa/student/{id}/semester/{semId} - should return 0 for no enrollments")
    void calculateSGPAWithNoEnrollments() throws Exception {
        mockMvc.perform(get("/api/sgpa/student/" + studentId + "/semester/" + semester2Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("No enrollments")));
    }

    @Test
    @DisplayName("GET /api/sgpa/student/{id}/semester/{semId} - should return 404 for non-existent student")
    void calculateSGPAWithInvalidStudent() throws Exception {
        mockMvc.perform(get("/api/sgpa/student/99999/semester/" + semester1Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STUDENT_NOT_FOUND"));
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.cgpa").isNumber());
    }

    @Test
    @DisplayName("GET /api/cgpa/student/{id} - should return 0 for no enrollments")
    void calculateOverallCGPAWithNoEnrollments() throws Exception {
        mockMvc.perform(get("/api/cgpa/student/" + studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cgpa").value(0.0))
                .andExpect(jsonPath("$.data.totalCredits").value(0));
    }

    @Test
    @DisplayName("GET /api/cgpa/student/{id}/semester/{semId} - should calculate CGPA up to semester")
    void calculateCGPAUpToSemester() throws Exception {
        // Create enrollment in semester 1
        createEnrollment(studentId, course1Id, "A");

        mockMvc.perform(get("/api/cgpa/student/" + studentId + "/semester/" + semester1Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cgpa").isNumber());
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Grade Scale Tests
    // ═══════════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/grades/scale - should return grading scale")
    void getGradingScale() throws Exception {
        mockMvc.perform(get("/api/grades/scale")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(8)); // O, A+, A, B+, B, C, P, F
    }

    @Test
    @DisplayName("GET /api/grades/from-marks/{marks} - should return grade for marks")
    void getGradeFromMarks() throws Exception {
        mockMvc.perform(get("/api/grades/from-marks/85")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.letterGrade").value("A+"))
                .andExpect(jsonPath("$.data.gradePoints").value(9));
    }

    @Test
    @DisplayName("GET /api/grades/from-marks/{marks} - should return F for low marks")
    void getGradeFromLowMarks() throws Exception {
        mockMvc.perform(get("/api/grades/from-marks/30")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterGrade").value("F"))
                .andExpect(jsonPath("$.data.gradePoints").value(0));
    }

    @Test
    @DisplayName("GET /api/grades/from-marks/{marks} - should return O for high marks")
    void getGradeFromHighMarks() throws Exception {
        mockMvc.perform(get("/api/grades/from-marks/95")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterGrade").value("O"))
                .andExpect(jsonPath("$.data.gradePoints").value(10));
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
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asLong();
    }
}
