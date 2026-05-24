package com.gradecalculator.controller;

import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.TranscriptPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradecalculator.config.SecurityConfig;
import com.gradecalculator.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = TranscriptController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class TranscriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private TranscriptPdfService transcriptPdfService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal facultyPrincipal;
    private UserPrincipal studentPrincipal;

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

        reset(transcriptPdfService);
    }

    @Test
    void downloadTranscriptPermittedForStaff() throws Exception {
        byte[] pdfBytes = "Dummy PDF Content".getBytes();
        when(transcriptPdfService.generateTranscript(eq(3L))).thenReturn(pdfBytes);

        // Admin: Success with PDF Headers
        mockMvc.perform(get("/api/transcript/pdf/{studentId}", 3L)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"transcript-3.pdf\""))
                .andExpect(content().bytes(pdfBytes));

        // Faculty: Success
        mockMvc.perform(get("/api/transcript/pdf/{studentId}", 3L)
                        .with(user(facultyPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void downloadTranscriptForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/transcript/pdf/{studentId}", 3L)
                        .with(user(studentPrincipal)))
                .andExpect(status().isForbidden());
    }
}
