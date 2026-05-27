package com.gradecalculator.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a course offered in a specific semester.
 */
@Entity
@Table(name = "courses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_code_semester", columnNames = {"course_code", "semester_id"})
}, indexes = {
        @Index(name = "idx_course_semester_id", columnList = "semester_id"),
        @Index(name = "idx_course_faculty_id", columnList = "faculty_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", nullable = false)
    @NonNull
    private String courseCode;

    @Column(name = "course_name", nullable = false)
    @NonNull
    private String courseName;

    @Column(nullable = false)
    @NonNull
    private Integer credits;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false)
    @Builder.Default
    private CourseType courseType = CourseType.THEORY;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private AppUser faculty;

    // Custom constructors
    public Course(String courseCode, String courseName, Integer credits) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.courseType = CourseType.THEORY;
    }

    public Course(String courseCode, String courseName, Integer credits, CourseType courseType) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.courseType = courseType != null ? courseType : CourseType.THEORY;
    }

    // Convenience methods matching existing code expectations
    public String getCode() {
        return courseCode;
    }

    public String getName() {
        return courseName;
    }
}
