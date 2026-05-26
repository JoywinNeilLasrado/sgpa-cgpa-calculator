package com.gradecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.request.LoginRequest;
import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.security.JwtTokenProvider;
import com.gradecalculator.security.LoginRateLimiterService;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.security.UserDetailsServiceImpl;
import com.gradecalculator.service.StudentService;
import com.gradecalculator.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gradecalculator.config.SecurityConfig;
import com.gradecalculator.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private StudentService studentService;
    @MockitoBean private LoginRateLimiterService rateLimiter;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        reset(rateLimiter, userService, studentService);
    }

    @Test
    void loginSucceedsWithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Password123!");

        AppUser user = new AppUser(1L, "admin", "encodedPassword", AppUser.Role.ADMIN, "Admin User", "admin@example.com", "CS");

        when(rateLimiter.isBlocked(anyString())).thenReturn(false);
        when(rateLimiter.isAccountLocked("admin")).thenReturn(false);
        when(userService.authenticate("admin", "Password123!")).thenReturn("mock-jwt-token");
        when(userService.findByUsername("admin")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));

        verify(rateLimiter).loginSucceeded(anyString());
        verify(rateLimiter).accountLoginSucceeded("admin");
    }

    @Test
    void loginBlockedByRateLimiter() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Password123!");

        when(rateLimiter.isBlocked(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests()); // RateLimitException mapping checks
    }

    @Test
    void loginBlockedByAccountLockout() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Password123!");

        when(rateLimiter.isBlocked(anyString())).thenReturn(false);
        when(rateLimiter.isAccountLocked("admin")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void registerSucceedsForAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("Pass1234!");
        request.setRole(AppUser.Role.STUDENT);

        AppUser savedUser = new AppUser(2L, "newUser", "encoded", AppUser.Role.STUDENT, "New User", null, null);
        when(userService.register(any(RegisterRequest.class))).thenReturn(savedUser);

        AppUser adminUser = new AppUser(1L, "admin", "encoded", AppUser.Role.ADMIN, "Admin", null, null);
        UserPrincipal adminPrincipal = new UserPrincipal(adminUser);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void registerForbiddenForNonAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("Pass1234!");
        request.setRole(AppUser.Role.STUDENT);

        AppUser studentUser = new AppUser(2L, "student", "encoded", AppUser.Role.STUDENT, "Student", null, null);
        UserPrincipal studentPrincipal = new UserPrincipal(studentUser);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentUserReturnsAuthenticatedUser() throws Exception {
        AppUser user = new AppUser(1L, "admin", "encoded", AppUser.Role.ADMIN, "Admin", null, null);
        UserPrincipal principal = new UserPrincipal(user);

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void changePasswordInvokesServiceSuccessfully() throws Exception {
        AuthController.ChangePasswordRequest request = new AuthController.ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPassword123!");

        AppUser user = new AppUser(1L, "admin", "encoded", AppUser.Role.ADMIN, "Admin", null, null);
        UserPrincipal principal = new UserPrincipal(user);

        mockMvc.perform(post("/api/auth/password")
                        .with(csrf())
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).changePassword(1L, "oldPass", "newPassword123!");
    }
}
