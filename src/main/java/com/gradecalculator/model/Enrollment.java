package com.gradecalculator.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity representing a student's enrollment in a course for a semester,
 * including the grade received.
 */
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonBackReference
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private Course course;

    @Enumerated(EnumType.STRING)
    private LetterGrade grade;

    // Constructors
    public Enrollment() {}

    public Enrollment(Student student, Course course, LetterGrade grade) {
        this.student = student;
        this.course = course;
        this.grade = grade;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LetterGrade getGrade() {
        return grade;
    }

    public void setGrade(LetterGrade grade) {
        this.grade = grade;
    }

    /**
     * Calculate credit points for this enrollment.
     * Credit Points = Course Credits × Grade Points
     */
    public int getCreditPoints() {
        if (course == null || course.getCredits() == null || grade == null) {
            return 0;
        }
        return course.getCredits() * grade.getGradePoints();
    }

    /**
     * Check if this enrollment has a passing grade.
     */
    public boolean isPassing() {
        return grade != null && grade != LetterGrade.F;
    }
}
