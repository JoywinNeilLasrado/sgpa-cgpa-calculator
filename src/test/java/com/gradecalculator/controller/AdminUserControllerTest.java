package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.request.PasswordChangeRequest;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.SecurityExpressionEvaluator;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradecalculator.config.SecurityConfig;
import com.gradecalculator.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = AdminUserController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean(name = "sec") private SecurityExpressionEvaluator securityExpressionEvaluator;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal studentPrincipal;

    @BeforeEach
    void setUp() {
        com.gradecalculator.model.AppUser admin = new com.gradecalculator.model.AppUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(com.gradecalculator.model.AppUser.Role.ADMIN);
        adminPrincipal = new UserPrincipal(admin);

        com.gradecalculator.model.AppUser student = new com.gradecalculator.model.AppUser();
        student.setId(3L);
        student.setUsername("student");
        student.setRole(com.gradecalculator.model.AppUser.Role.STUDENT);
        studentPrincipal = new UserPrincipal(student);

        reset(userService);
    }

    @Test
    void changePasswordByUsernamePermittedForAdmin() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(null, "student", "Password123!");
        doNothing().when(userService).adminChangePasswordByUsername("student", "Password123!");

        mockMvc.perform(put("/api/admin/users/password")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).adminChangePasswordByUsername("student", "Password123!");
    }

    @Test
    void changePasswordByUserIdPermittedForAdmin() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(3L, "", "Password123!");
        doNothing().when(userService).adminChangePassword(3L, "Password123!");

        mockMvc.perform(put("/api/admin/users/password")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).adminChangePassword(3L, "Password123!");
    }

    @Test
    void changePasswordForbiddenForNonAdmin() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(3L, "student", "Password123!");

        mockMvc.perform(put("/api/admin/users/password")
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}
