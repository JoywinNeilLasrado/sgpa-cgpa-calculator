package com.gradecalculator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    Long userId,
    String username,
    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, message = "New password must be at least 6 characters")
    String newPassword
) {}
