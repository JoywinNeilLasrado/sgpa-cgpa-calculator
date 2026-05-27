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
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "courses"})
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private Integer semesterNumber;

    @OneToMany(mappedBy = "semester")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    @JsonIgnore
    public List<Course> getCourses() {
        return this.courses;
    }

    // Custom constructors
    public Semester(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
        this.courses = new ArrayList<>();
    }

    // Convenience methods
    public String getDisplayName() {
        return "Semester " + semesterNumber;
    }

    @JsonIgnore
    public int getCourseCount() {
        return courses != null ? courses.size() : 0;
    }
}