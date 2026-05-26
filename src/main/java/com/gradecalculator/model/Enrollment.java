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

    private Integer cieMarks; // Kept for backward compatibility
    private Integer cieTheoryMarks; // Kept for backward compatibility
    private Integer cieLabMarks; // Kept for backward compatibility
    
    // Detailed Theory Marks
    private Integer test1Marks;
    private Integer test2Marks;
    private Integer assignmentMarks;
    private Integer oaaMarks;
    
    // Detailed Lab Marks
    private Integer regularLabMarks;
    private Integer labTestMarks;
    private Integer labRecordMarks;

    private Integer seeMarks;
    private Integer graceMarks;
    private Integer totalMarks;

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

    public Integer getCieMarks() {
        return cieMarks;
    }

    public void setCieMarks(Integer cieMarks) {
        this.cieMarks = cieMarks;
    }

    public Integer getCieTheoryMarks() {
        return cieTheoryMarks;
    }

    public void setCieTheoryMarks(Integer cieTheoryMarks) {
        this.cieTheoryMarks = cieTheoryMarks;
    }

    public Integer getCieLabMarks() {
        return cieLabMarks;
    }

    public void setCieLabMarks(Integer cieLabMarks) {
        this.cieLabMarks = cieLabMarks;
    }

    public Integer getTest1Marks() { return test1Marks; }
    public void setTest1Marks(Integer test1Marks) { this.test1Marks = test1Marks; }

    public Integer getTest2Marks() { return test2Marks; }
    public void setTest2Marks(Integer test2Marks) { this.test2Marks = test2Marks; }

    public Integer getAssignmentMarks() { return assignmentMarks; }
    public void setAssignmentMarks(Integer assignmentMarks) { this.assignmentMarks = assignmentMarks; }

    public Integer getOaaMarks() { return oaaMarks; }
    public void setOaaMarks(Integer oaaMarks) { this.oaaMarks = oaaMarks; }

    public Integer getRegularLabMarks() { return regularLabMarks; }
    public void setRegularLabMarks(Integer regularLabMarks) { this.regularLabMarks = regularLabMarks; }

    public Integer getLabTestMarks() { return labTestMarks; }
    public void setLabTestMarks(Integer labTestMarks) { this.labTestMarks = labTestMarks; }

    public Integer getLabRecordMarks() { return labRecordMarks; }
    public void setLabRecordMarks(Integer labRecordMarks) { this.labRecordMarks = labRecordMarks; }

    public Integer getSeeMarks() {
        return seeMarks;
    }

    public void setSeeMarks(Integer seeMarks) {
        this.seeMarks = seeMarks;
    }

    public Integer getGraceMarks() {
        return graceMarks;
    }

    public void setGraceMarks(Integer graceMarks) {
        this.graceMarks = graceMarks;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public void calculateGrade() {
        if (course == null || course.getCourseType() == null) {
            return;
        }

        CourseType type = course.getCourseType();
        int finalCie = 0;
        // The SEE is initially conducted for a total of 100 marks and is then proportionally reduced to 50 marks.
        // If the user inputs the 50 marks, we just take it. If they input out of 100, we'd need to divide by 2.
        // The plan stated seeMarks out of 50. Let's assume seeMarks is already the reduced 50.
        int finalSee = (seeMarks != null ? seeMarks : 0) + (graceMarks != null ? graceMarks : 0);
        boolean passesCie = false;

        if (type == CourseType.THEORY) {
            int t1 = (test1Marks != null ? test1Marks : 0);
            int t2 = (test2Marks != null ? test2Marks : 0);
            int avgTests = (int) Math.round((t1 + t2) * 0.3); // Average of Test 1 and Test 2 (out of 100) scaled to 30
            int assign = (assignmentMarks != null ? assignmentMarks : 0);
            int oaa = (oaaMarks != null ? oaaMarks : 0);
            finalCie = avgTests + assign + oaa;
            this.cieMarks = finalCie;
            passesCie = finalCie >= 20;
        } else if (type == CourseType.LABORATORY) {
            int reg = (regularLabMarks != null ? regularLabMarks : 0);
            int test = (labTestMarks != null ? labTestMarks : 0);
            int rec = (labRecordMarks != null ? labRecordMarks : 0);
            finalCie = reg + test + rec;
            this.cieMarks = finalCie;
            passesCie = finalCie >= 25;
        } else if (type == CourseType.INTEGRATED) {
            int t1 = (test1Marks != null ? test1Marks : 0);
            int t2 = (test2Marks != null ? test2Marks : 0);
            int avgTests = (int) Math.round((t1 + t2) * 0.3); // Average of Test 1 and Test 2 scaled to 30
            int assign = (assignmentMarks != null ? assignmentMarks : 0);
            int oaa = (oaaMarks != null ? oaaMarks : 0);
            double theoryTotal = avgTests + assign + oaa;
            int theoryReduced = (int) Math.round(theoryTotal * 0.6); // Total theory out of 50 reduced to 30
            
            int reg = (regularLabMarks != null ? regularLabMarks : 0);
            int test = (labTestMarks != null ? labTestMarks : 0);
            int rec = (labRecordMarks != null ? labRecordMarks : 0);
            int labTotal = reg + test + rec;
            int labReduced = (int) Math.round(labTotal * 0.4); // Total lab out of 50 reduced to 20
            
            this.cieTheoryMarks = theoryReduced;
            this.cieLabMarks = labReduced;
            finalCie = theoryReduced + labReduced; // Theory (30) + Lab (20) = 50
            this.cieMarks = finalCie;
            passesCie = theoryReduced >= 12 && labReduced >= 8;
        }

        this.totalMarks = finalCie + finalSee;

        boolean passesSee = finalSee >= 18;
        boolean overallPass = passesCie && passesSee && this.totalMarks >= 40;

        if (!overallPass) {
            this.grade = LetterGrade.F;
        } else {
            this.grade = LetterGrade.fromMarks(this.totalMarks);
        }
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
