package com.gradecalculator.controller;

import com.gradecalculator.dto.request.PasswordChangeRequest;
import com.gradecalculator.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/password")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changePassword(@jakarta.validation.Valid @RequestBody PasswordChangeRequest request) {
        if (request.username() != null && !request.username().trim().isEmpty()) {
            userService.adminChangePasswordByUsername(request.username(), request.newPassword());
        } else {
            userService.adminChangePassword(request.userId(), request.newPassword());
        }
        return ResponseEntity.ok("Password updated");
    }
}

