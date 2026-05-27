package com.gradecalculator.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an academic department.
 */
@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NonNull
    private String name;

    @Column(nullable = false, unique = true)
    @NonNull
    private String code;

    // Custom constructors
    public Department(@NonNull String name, @NonNull String code) {
        this.name = name;
        this.code = code;
    }
}
