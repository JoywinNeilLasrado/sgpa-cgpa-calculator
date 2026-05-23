package com.gradecalculator.controller;

import com.gradecalculator.dto.request.LoginRequest;
import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.dto.response.LoginResponse;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.service.UserService;
import com.gradecalculator.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final StudentService studentService;
    private final com.gradecalculator.security.LoginRateLimiterService rateLimiter;

    public AuthController(UserService userService, 
                          StudentService studentService,
                          com.gradecalculator.security.LoginRateLimiterService rateLimiter) {
        this.userService = userService;
        this.studentService = studentService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Login - POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        if (rateLimiter.isBlocked(ip)) {
            logger.warn("Security Event: Blocked login attempt from IP: '{}' due to rate limiting", ip);
            throw new com.gradecalculator.exception.RateLimitException("Too many failed login attempts. Please try again after 15 minutes.");
        }

        try {
            String token = userService.authenticate(request.getUsername(), request.getPassword());
            rateLimiter.loginSucceeded(ip);
            
            AppUser user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("AppUser not found"));

            Long userId = user.getId();
            if (user.getRole() == AppUser.Role.STUDENT) {
                userId = studentService.findByUsername(user.getUsername())
                        .map(com.gradecalculator.model.Student::getId)
                        .orElse(user.getId());
            }

            logger.info("Security Event: Successful login for username: '{}' from IP: '{}'", request.getUsername(), ip);

            return ResponseEntity.ok(new LoginResponse(
                    userId,
                    user.getUsername(),
                    user.getRole().name(),
                    token
            ));
        } catch (org.springframework.security.core.AuthenticationException e) {
            logger.warn("Security Event: Failed login attempt for username: '{}' from IP: '{}'", request.getUsername(), ip);
            rateLimiter.loginFailed(ip);
            throw e;
        }
    }

    /**
     * Register - POST /api/auth/register (Admin only initially)
     */
    @PostMapping("/register")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUser> register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = userService.register(request);
        return ResponseEntity.ok(user);
    }

    /**
     * Get current user - GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        AppUser user = userService.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("AppUser not found"));

        Long userId = user.getId();
        if (user.getRole() == AppUser.Role.STUDENT) {
            userId = studentService.findByUsername(user.getUsername())
                    .map(com.gradecalculator.model.Student::getId)
                    .orElse(user.getId());
        }

        return ResponseEntity.ok(new LoginResponse(
                userId,
                user.getUsername(),
                user.getRole().name(),
                null  // Don't return token for /me
        ));
    }

    /**
     * Change password - PUT /api/auth/change-password
     */
    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        userService.changePassword(principal.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Inner class for change password request
     */
    public static class ChangePasswordRequest {
        @jakarta.validation.constraints.NotBlank(message = "Old password cannot be empty")
        private String oldPassword;

        @jakarta.validation.constraints.NotBlank(message = "New password cannot be empty")
        @jakarta.validation.constraints.Size(min = 6, message = "New password must be at least 6 characters")
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}