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
import jakarta.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity representing a student's enrollment in a course for a semester,
 * including the grade received.
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_course_enrollment", columnNames = {"student_id", "course_id"})
}, indexes = {
        @jakarta.persistence.Index(name = "idx_enrollment_student_id", columnList = "student_id"),
        @jakarta.persistence.Index(name = "idx_enrollment_course_id", columnList = "course_id")
})
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

    @jakarta.persistence.Embedded
    private EnrollmentMarks marks = new EnrollmentMarks();

    @jakarta.persistence.Version
    private Long version;

    private String lastModifiedBy;

    private java.time.LocalDateTime lastModifiedAt;

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public java.time.LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(java.time.LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }

    // Constructors
    public Enrollment() {}

    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
    }

    public Enrollment(Student student, Course course, LetterGrade grade) {
        this.student = student;
        this.course = course;
        this.grade = grade; // Kept for backwards compatibility
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

    public Integer getCieMarks() { return marks.getCieMarks(); }
    public void setCieMarks(Integer cieMarks) { marks.setCieMarks(cieMarks); }

    public Integer getCieTheoryMarks() { return marks.getCieTheoryMarks(); }
    public void setCieTheoryMarks(Integer cieTheoryMarks) { marks.setCieTheoryMarks(cieTheoryMarks); }

    public Integer getCieLabMarks() { return marks.getCieLabMarks(); }
    public void setCieLabMarks(Integer cieLabMarks) { marks.setCieLabMarks(cieLabMarks); }

    public Integer getTest1Marks() { return marks.getTest1Marks(); }
    public void setTest1Marks(Integer test1Marks) { marks.setTest1Marks(test1Marks); }

    public Integer getTest2Marks() { return marks.getTest2Marks(); }
    public void setTest2Marks(Integer test2Marks) { marks.setTest2Marks(test2Marks); }

    public Integer getAssignmentMarks() { return marks.getAssignmentMarks(); }
    public void setAssignmentMarks(Integer assignmentMarks) { marks.setAssignmentMarks(assignmentMarks); }

    public Integer getOaaMarks() { return marks.getOaaMarks(); }
    public void setOaaMarks(Integer oaaMarks) { marks.setOaaMarks(oaaMarks); }

    public Integer getRegularLabMarks() { return marks.getRegularLabMarks(); }
    public void setRegularLabMarks(Integer regularLabMarks) { marks.setRegularLabMarks(regularLabMarks); }

    public Integer getLabTestMarks() { return marks.getLabTestMarks(); }
    public void setLabTestMarks(Integer labTestMarks) { marks.setLabTestMarks(labTestMarks); }

    public Integer getLabRecordMarks() { return marks.getLabRecordMarks(); }
    public void setLabRecordMarks(Integer labRecordMarks) { marks.setLabRecordMarks(labRecordMarks); }

    public Integer getSeeMarks() { return marks.getSeeMarks(); }
    public void setSeeMarks(Integer seeMarks) { marks.setSeeMarks(seeMarks); }

    public Integer getGraceMarks() { return marks.getGraceMarks(); }
    public void setGraceMarks(Integer graceMarks) { marks.setGraceMarks(graceMarks); }

    public Integer getTotalMarks() { return marks.getTotalMarks(); }
    public void setTotalMarks(Integer totalMarks) { marks.setTotalMarks(totalMarks); }

    public void calculateGrade() {
        if (course == null || course.getCourseType() == null) {
            return;
        }
        this.grade = marks.calculateGrade(course.getCourseType());
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
