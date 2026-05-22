package com.gradecalculator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a student.
 */
@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_roll_number", columnNames = "student_id")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "student_id", nullable = false)
    private String studentId; // University roll number

    @Column(name = "branch")
    private String branch = "Computer Science";

    @OneToMany(mappedBy = "student")
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

    // Constructors
    public Student() {}

    public Student(String name, String studentId) {
        this.name = name;
        this.studentId = studentId;
        this.branch = "Computer Science";
    }

    public Student(String name, String studentId, String branch) {
        this.name = name;
        this.studentId = studentId;
        this.branch = branch != null && !branch.trim().isEmpty() ? branch : "Computer Science";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch != null && !branch.trim().isEmpty() ? branch : "Computer Science";
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
}
