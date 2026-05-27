package com.gradecalculator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a semester.
 */
@Entity
@Table(name = "semesters")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private Integer semesterNumber;

    @OneToMany(mappedBy = "semester")
    @JsonIgnore
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    // Custom constructors
    public Semester(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    // Convenience methods
    public String getDisplayName() {
        return "Semester " + semesterNumber;
    }

    public int getCourseCount() {
        return courses != null ? courses.size() : 0;
    }
}