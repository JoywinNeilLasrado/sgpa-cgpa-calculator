package com.gradecalculator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.request.LoginRequest;
import com.gradecalculator.model.AuditLog;
import com.gradecalculator.repository.AuditLogRepository;
import com.gradecalculator.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "app.demo.password=adminPassword123",
    "app.demo.seed=true",
    "jwt.secret=MySuperSecretKey1234567890123456MySuperSecretKey1234567890123456",
    "app.api.key=my-test-api-key",
    "app.throttle.limit=5",
    "app.throttle.window-ms=60000"
})
@AutoConfigureMockMvc
@Transactional
public class SecurityQualityElevationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    public void testJwtSecretKeyLengthCheck() {
        // Enforce that empty key throws exception
        assertThatThrownBy(() -> new JwtTokenProvider("", 86400000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("signing secret key is not configured");

        // Enforce that weak key throws exception
        assertThatThrownBy(() -> new JwtTokenProvider("short-key", 86400000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be at least 32 characters long");
    }

    @Test
    public void testApiKeyInternalAuthentication() throws Exception {
        // 1. Request without X-API-KEY should fail (401 Unauthorized)
        mockMvc.perform(get("/api/internal/system-status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Missing or invalid API key in header X-API-KEY."));

        // 2. Request with invalid key should fail (401 Unauthorized)
        mockMvc.perform(get("/api/internal/system-status")
                        .header("X-API-KEY", "wrong-key"))
                .andExpect(status().isUnauthorized());

        // 3. Request with valid key should succeed (200 OK)
        mockMvc.perform(get("/api/internal/system-status")
                        .header("X-API-KEY", "my-test-api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.serviceName").value("sgpa-cgpa-calculator-internal"));
    }

    @Test
    public void testAuditLogRelationalDatabasePersistence() {
        long initialCount = auditLogRepository.count();

        // Save a mock audit log entry to database
        AuditLog log = new AuditLog(
                java.time.LocalDateTime.now(),
                "test-user",
                "127.0.0.1",
                "CREATE",
                "Course",
                "101",
                "None",
                "code=CS101"
        );
        auditLogRepository.save(log);

        assertThat(auditLogRepository.count()).isEqualTo(initialCount + 1);
        List<AuditLog> list = auditLogRepository.findByEntityNameAndEntityId("Course", "101");
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getUsername()).isEqualTo("test-user");
    }

    @Test
    public void testLoginCaptchaChallengeVerification() throws Exception {
        LoginRequest badRequest = new LoginRequest();
        badRequest.setUsername("captcha-user");
        badRequest.setPassword("wrong-password");

        // 1. Failed logins 1 and 2: no CAPTCHA required yet
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.requiresCaptcha").value(false));
        }

        // 2. Failed login 3: triggers CAPTCHA for subsequent requests
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.requiresCaptcha").value(true));

        // 3. Next login attempts without CAPTCHA are blocked immediately
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.requiresCaptcha").value(true))
                .andExpect(jsonPath("$.message").value("CAPTCHA verification required or invalid CAPTCHA."));

        // 4. Next login attempt with valid CAPTCHA will bypass the CAPTCHA block (though still fails on password credentials)
        badRequest.setCaptcha("VALID_CAPTCHA");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    public void testRequestThrottlingFilter() throws Exception {
        // Perform multiple rapid requests to trigger throttling
        // The throttle limit is configured to 5 in properties for this test
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/grade-scale"))
                    .andExpect(status().isOk());
        }

        // The 6th request should be throttled (429 Too Many Requests)
        mockMvc.perform(get("/api/grade-scale"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("TOO_MANY_REQUESTS"))
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }
}
