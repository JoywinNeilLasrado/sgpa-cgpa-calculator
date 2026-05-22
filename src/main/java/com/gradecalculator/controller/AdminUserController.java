package com.gradecalculator.controller;

import com.gradecalculator.dto.PasswordChangeRequest;
import com.gradecalculator.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            userService.adminChangePasswordByUsername(request.getUsername(), request.getNewPassword());
        } else {
            userService.adminChangePassword(request.getUserId(), request.getNewPassword());
        }
        return ResponseEntity.ok("Password updated");
    }
}
