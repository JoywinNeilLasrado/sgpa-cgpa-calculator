package com.gradecalculator.model;

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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;  // Same as studentId

    @Column(nullable = false)
    private String password;  // BCrypt encoded

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user")
    private Student student;

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