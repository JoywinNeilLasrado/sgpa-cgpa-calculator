package com.gradecalculator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.gradecalculator.model.AppUser.Role;

/**
 * Register request DTO
 */
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters long")
    @jakarta.validation.constraints.Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character (@#$%^&+=!)"
    )
    private String password;

    @jakarta.validation.constraints.Email(message = "Please provide a valid email address")
    private String email;

    private String name;

    private String department;

    @NotNull(message = "Role is required")
    private Role role;

    private Long studentId;  // Optional: link to student record

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

}
