package com.gradecalculator.controller;

import com.gradecalculator.dto.request.LoginRequest;
import com.gradecalculator.dto.request.RegisterRequest;
import com.gradecalculator.dto.response.LoginResponse;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.security.UserPrincipal;
import com.gradecalculator.service.UserService;
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

    private final UserService userService;
    private final com.gradecalculator.repository.StudentRepository studentRepository;

    public AuthController(UserService userService, com.gradecalculator.repository.StudentRepository studentRepository) {
        this.userService = userService;
        this.studentRepository = studentRepository;
    }

    /**
     * Login - POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.authenticate(request.getUsername(), request.getPassword());
        
        AppUser user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("AppUser not found"));

        Long userId = user.getId();
        if (user.getRole() == AppUser.Role.STUDENT) {
            userId = studentRepository.findByUsername(user.getUsername())
                    .map(com.gradecalculator.model.Student::getId)
                    .orElse(user.getId());
        }

        return ResponseEntity.ok(new LoginResponse(
                userId,
                user.getUsername(),
                user.getRole().name(),
                token
        ));
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
            userId = studentRepository.findByUsername(user.getUsername())
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