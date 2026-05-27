package com.gradecalculator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a student.
 */
@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_roll_number", columnNames = "student_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "enrollments"})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String name;

    @Column(name = "student_id", nullable = false)
    @NonNull
    private String studentId; // University roll number

    @Column(name = "branch")
    @Builder.Default
    private String branch = "Computer Science";

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @OneToMany(mappedBy = "student")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @JsonIgnore
    public List<Enrollment> getEnrollments() {
        return this.enrollments;
    }

    // Custom constructors for specific use cases
    public Student(@NonNull String name, @NonNull String studentId) {
        this.name = name;
        this.studentId = studentId;
        this.branch = "Computer Science";
        this.enrollments = new ArrayList<>();
    }

    public Student(@NonNull String name, @NonNull String studentId, String branch) {
        this.name = name;
        this.studentId = studentId;
        this.branch = branch != null && !branch.trim().isEmpty() ? branch : "Computer Science";
        this.enrollments = new ArrayList<>();
    }

    /**
     * Helper method to add an enrollment (bidirectional relationship).
     */
    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setStudent(this);
    }

    /**
     * Helper method to remove an enrollment (bidirectional relationship).
     */
    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setStudent(null);
    }
}
