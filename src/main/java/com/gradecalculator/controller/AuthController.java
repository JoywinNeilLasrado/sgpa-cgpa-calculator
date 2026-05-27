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
     * Returns structured JSON including attempt count and lockout info for UI feedback.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String username = request.getUsername();

        // Check if IP or account is locked — return remaining lockout time
        if (rateLimiter.isBlocked(ip) || rateLimiter.isAccountLocked(username)) {
            long remainingMs = rateLimiter.getAccountLockoutRemainingMs(username);
            logger.warn("Security Event: Blocked login attempt for '{}' from IP: '{}'", username, ip);
            return ResponseEntity.status(429).body(new LoginErrorResponse(
                    "Account locked due to too many failed attempts.",
                    com.gradecalculator.security.LoginRateLimiterService.MAX_ATTEMPTS,
                    com.gradecalculator.security.LoginRateLimiterService.MAX_ATTEMPTS,
                    remainingMs,
                    true
            ));
        }

        int currentAttempts = rateLimiter.getAccountAttempts(username);
        boolean requiresCaptcha = currentAttempts >= 3;

        if (requiresCaptcha) {
            if (request.getCaptcha() == null || !request.getCaptcha().equals("VALID_CAPTCHA")) {
                logger.warn("Security Event: Blocked login due to missing/invalid CAPTCHA for '{}' from IP: '{}'", username, ip);
                rateLimiter.loginFailed(ip);
                rateLimiter.accountLoginFailed(username);

                int attempts = rateLimiter.getAccountAttempts(username);
                int remaining = Math.max(0, com.gradecalculator.security.LoginRateLimiterService.MAX_ATTEMPTS - attempts);
                long lockoutMs = rateLimiter.getAccountLockoutRemainingMs(username);

                return ResponseEntity.status(401).body(new LoginErrorResponse(
                        "CAPTCHA verification required or invalid CAPTCHA.",
                        attempts,
                        remaining,
                        lockoutMs,
                        true
                ));
            }
        }

        try {
            String token = userService.authenticate(request.getUsername(), request.getPassword());
            rateLimiter.loginSucceeded(ip);
            rateLimiter.accountLoginSucceeded(username);

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
                    token,
                    user.isMustChangePassword()
            ));
        } catch (org.springframework.security.core.AuthenticationException e) {
            logger.warn("Security Event: Failed login attempt for username: '{}' from IP: '{}'", request.getUsername(), ip);
            rateLimiter.loginFailed(ip);
            rateLimiter.accountLoginFailed(username);

            int attempts = rateLimiter.getAccountAttempts(username);
            int remaining = Math.max(0, com.gradecalculator.security.LoginRateLimiterService.MAX_ATTEMPTS - attempts);
            long lockoutMs = rateLimiter.getAccountLockoutRemainingMs(username);
            boolean nextRequiresCaptcha = attempts >= 3;

            return ResponseEntity.status(401).body(new LoginErrorResponse(
                    "Invalid username or password.",
                    attempts,
                    remaining,
                    lockoutMs,
                    nextRequiresCaptcha
            ));
        }
    }

    /** Structured error response with rate-limit feedback for the login UI. */
    public record LoginErrorResponse(
            String message,
            int attemptsMade,
            int attemptsRemaining,
            long lockoutRemainingMs,
            boolean requiresCaptcha
    ) {}

    /**
     * Register - POST /api/auth/register (Admin only initially)
     */
    @PostMapping("/register")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        
        // Brute-force check on registration endpoint
        if (rateLimiter.isBlocked(ip)) {
            logger.warn("Security Event: Blocked registration attempt from IP: '{}' due to excessive attempts", ip);
            return ResponseEntity.status(429).body(new com.gradecalculator.dto.ErrorResponse(
                    "TOO_MANY_REQUESTS",
                    429,
                    "Registration blocked due to too many failed security requests.",
                    httpRequest.getRequestURI(),
                    java.time.LocalDateTime.now(),
                    java.util.List.of("Brute-force protection activated on registration.")
            ));
        }

        try {
            AppUser user = userService.register(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // Track failed registration attempt
            rateLimiter.loginFailed(ip);
            throw e; // rethrow so GlobalExceptionHandler handles it
        }
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
                null,  // Don't return token for /me
                user.isMustChangePassword()
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
     * Change password - PUT /api/auth/change-password (Standard PUT support for frontend UI)
     */
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePasswordPut(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        userService.changePassword(principal.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
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