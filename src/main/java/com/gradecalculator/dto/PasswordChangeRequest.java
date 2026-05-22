package com.gradecalculator.dto;

public class PasswordChangeRequest {
    private Long userId; // student or faculty ID
    private String username;
    private String newPassword;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
