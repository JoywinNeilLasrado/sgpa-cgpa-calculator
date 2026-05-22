package com.gradecalculator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * User entity for authentication and authorization
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    // Getters and Setters for new fields
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Column(nullable = false)
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;  // Same as studentId

    @JsonIgnore
    @Column(nullable = false)
    private String password;  // BCrypt encoded

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private String email;
    private String department;

    /**
     * User roles for authorization
     */
    public enum Role {
        ADMIN,
        FACULTY,
        STUDENT
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isFaculty() {
        return role == Role.FACULTY;
    }

    public boolean isStudent() {
        return role == Role.STUDENT;
    }
}