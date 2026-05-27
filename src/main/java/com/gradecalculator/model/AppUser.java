package com.gradecalculator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * User entity for authentication and authorization
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NonNull
    private String username;  // Same as studentId

    @JsonIgnore
    @Column(nullable = false)
    @NonNull
    private String password;  // BCrypt encoded

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NonNull
    private Role role;

    @Column(nullable = false)
    @NonNull
    private String name;

    private String email;
    private String department;

    @Column(nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;

    // Custom constructors
    public AppUser(Long id, String username, String password, Role role, String name, String email, String department) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.department = department;
    }

    // Convenience methods for role checks
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isFaculty() {
        return role == Role.FACULTY;
    }

    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    /**
     * User roles for authorization
     */
    public enum Role {
        ADMIN,
        FACULTY,
        STUDENT
    }
}