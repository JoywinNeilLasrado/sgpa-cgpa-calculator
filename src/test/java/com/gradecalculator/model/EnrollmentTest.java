package com.gradecalculator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentTest {

    private Student student;
    private Course theoryCourse;
    private Course labCourse;
    private Course integratedCourse;

    @BeforeEach
    void setUp() {
        student = new Student("Alice Johnson", "CS2024001");
        student.setId(1L);

        theoryCourse = new Course("CS101", "Theory Course", 4);
        theoryCourse.setCourseType(CourseType.THEORY);

        labCourse = new Course("CS101L", "Laboratory Course", 2);
        labCourse.setCourseType(CourseType.LABORATORY);

        integratedCourse = new Course("CS102", "Integrated Course", 4);
        integratedCourse.setCourseType(CourseType.INTEGRATED);
    }

    @Test
    void calculateGradeTheoryCoursePass() {
        Enrollment enrollment = new Enrollment(student, theoryCourse, null);
        // CIE: Test 1 (50), Test 2 (50) -> scaled to 30: (50 + 50) * 0.3 = 30
        enrollment.setTest1Marks(50);
        enrollment.setTest2Marks(50);
        enrollment.setAssignmentMarks(10);
        enrollment.setOaaMarks(10);
        
        // SEE: 40 marks (out of 50)
        enrollment.setSeeMarks(40);
        enrollment.setGraceMarks(0);

        enrollment.calculateGrade();

        // CIE: 30 + 10 + 10 = 50
        assertThat(enrollment.getCieMarks()).isEqualTo(50);
        // SEE: 40
        assertThat(enrollment.getSeeMarks()).isEqualTo(40);
        // Total: 50 + 40 = 90 -> Grade O (Outstanding, 10 GP)
        assertThat(enrollment.getTotalMarks()).isEqualTo(90);
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.O);
        assertThat(enrollment.getCreditPoints()).isEqualTo(40);
        assertThat(enrollment.isPassing()).isTrue();
    }

    @Test
    void calculateGradeTheoryCourseFailCie() {
        Enrollment enrollment = new Enrollment(student, theoryCourse, null);
        // CIE: Test 1 (20), Test 2 (20) -> scaled to 30: (20 + 20) * 0.3 = 12
        enrollment.setTest1Marks(20);
        enrollment.setTest2Marks(20);
        enrollment.setAssignmentMarks(3);
        enrollment.setOaaMarks(4);
        
        // SEE: 40 marks (out of 50)
        enrollment.setSeeMarks(40);

        enrollment.calculateGrade();

        // CIE: 12 + 3 + 4 = 19 (Less than 20 -> Fail CIE)
        assertThat(enrollment.getCieMarks()).isEqualTo(19);
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.F);
        assertThat(enrollment.isPassing()).isFalse();
    }

    @Test
    void calculateGradeLaboratoryCoursePass() {
        Enrollment enrollment = new Enrollment(student, labCourse, null);
        // CIE: Reg Lab (20), Lab Test (20), Lab Record (10)
        enrollment.setRegularLabMarks(20);
        enrollment.setLabTestMarks(20);
        enrollment.setLabRecordMarks(10);
        
        // SEE: 30 marks
        enrollment.setSeeMarks(30);

        enrollment.calculateGrade();

        // CIE: 20 + 20 + 10 = 50 (>= 25 passes CIE)
        assertThat(enrollment.getCieMarks()).isEqualTo(50);
        // Total: 50 + 30 = 80 -> Grade A+ (Excellent, 9 GP)
        assertThat(enrollment.getTotalMarks()).isEqualTo(80);
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.A_PLUS);
        assertThat(enrollment.getCreditPoints()).isEqualTo(18);
        assertThat(enrollment.isPassing()).isTrue();
    }

    @Test
    void calculateGradeLaboratoryCourseFailCie() {
        Enrollment enrollment = new Enrollment(student, labCourse, null);
        // CIE: Reg Lab (10), Lab Test (10), Lab Record (4)
        enrollment.setRegularLabMarks(10);
        enrollment.setLabTestMarks(10);
        enrollment.setLabRecordMarks(4);
        
        // SEE: 30 marks
        enrollment.setSeeMarks(30);

        enrollment.calculateGrade();

        // CIE: 10 + 10 + 4 = 24 (Less than 25 -> Fail CIE)
        assertThat(enrollment.getCieMarks()).isEqualTo(24);
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.F);
        assertThat(enrollment.isPassing()).isFalse();
    }

    @Test
    void calculateGradeIntegratedCoursePass() {
        Enrollment enrollment = new Enrollment(student, integratedCourse, null);
        // Theory component: Test 1 (50), Test 2 (50) -> avg scaled: 30. Assign (10), OAA (10). Total = 50. Reduced to 30 = 30
        enrollment.setTest1Marks(50);
        enrollment.setTest2Marks(50);
        enrollment.setAssignmentMarks(10);
        enrollment.setOaaMarks(10);

        // Lab component: Reg Lab (20), Lab Test (20), Lab Record (10). Total = 50. Reduced to 20 = 20
        enrollment.setRegularLabMarks(20);
        enrollment.setLabTestMarks(20);
        enrollment.setLabRecordMarks(10);
        
        // SEE: 30 marks
        enrollment.setSeeMarks(30);

        enrollment.calculateGrade();

        // Theory component: Math.round(50 * 0.6) = 30
        assertThat(enrollment.getCieTheoryMarks()).isEqualTo(30);
        // Lab component: Math.round(50 * 0.4) = 20
        assertThat(enrollment.getCieLabMarks()).isEqualTo(20);
        // Total CIE: 30 + 20 = 50 (passes CIE because 30 >= 12 and 20 >= 8)
        assertThat(enrollment.getCieMarks()).isEqualTo(50);
        // Total Marks: 50 + 30 = 80 -> A+
        assertThat(enrollment.getTotalMarks()).isEqualTo(80);
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.A_PLUS);
        assertThat(enrollment.isPassing()).isTrue();
    }

    @Test
    void calculateGradeIntegratedCourseFailTheoryComponent() {
        Enrollment enrollment = new Enrollment(student, integratedCourse, null);
        // Theory component: Test 1 (20), Test 2 (20) -> avg scaled: 12. Assign (3), OAA (4). Total = 19. Reduced to 30 = Math.round(19 * 0.6) = 11
        enrollment.setTest1Marks(20);
        enrollment.setTest2Marks(20);
        enrollment.setAssignmentMarks(3);
        enrollment.setOaaMarks(4);

        // Lab component: Reg Lab (20), Lab Test (20), Lab Record (10). Total = 50. Reduced to 20 = 20
        enrollment.setRegularLabMarks(20);
        enrollment.setLabTestMarks(20);
        enrollment.setLabRecordMarks(10);
        
        // SEE: 30 marks
        enrollment.setSeeMarks(30);

        enrollment.calculateGrade();

        // Theory component: 11 (Less than 12 -> Fail Theory Component)
        assertThat(enrollment.getCieTheoryMarks()).isEqualTo(11);
        assertThat(enrollment.getCieLabMarks()).isEqualTo(20);
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.F);
    }

    @Test
    void calculateGradeIntegratedCourseFailLabComponent() {
        Enrollment enrollment = new Enrollment(student, integratedCourse, null);
        // Theory component: Test 1 (50), Test 2 (50) -> avg: 30. Assign (10), OAA (10). Total = 50. Reduced to 30 = 30
        enrollment.setTest1Marks(50);
        enrollment.setTest2Marks(50);
        enrollment.setAssignmentMarks(10);
        enrollment.setOaaMarks(10);

        // Lab component: Reg Lab (6), Lab Test (6), Lab Record (5). Total = 17. Reduced to 20 = Math.round(17 * 0.4) = 7
        enrollment.setRegularLabMarks(6);
        enrollment.setLabTestMarks(6);
        enrollment.setLabRecordMarks(5);
        
        // SEE: 30 marks
        enrollment.setSeeMarks(30);

        enrollment.calculateGrade();

        // Lab component: 7 (Less than 8 -> Fail Lab Component)
        assertThat(enrollment.getCieTheoryMarks()).isEqualTo(30);
        assertThat(enrollment.getCieLabMarks()).isEqualTo(7);
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.F);
    }

    @Test
    void calculateGradeFailsWhenSeeFails() {
        Enrollment enrollment = new Enrollment(student, theoryCourse, null);
        // CIE: 50 (Passes)
        enrollment.setTest1Marks(50);
        enrollment.setTest2Marks(50);
        enrollment.setAssignmentMarks(10);
        enrollment.setOaaMarks(10);
        
        // SEE: 17 (Fails SEE, threshold is >= 18)
        enrollment.setSeeMarks(17);

        enrollment.calculateGrade();

        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.F);
    }

    @Test
    void calculateGradePassesWithGraceMarks() {
        Enrollment enrollment = new Enrollment(student, theoryCourse, null);
        // CIE: 50 (Passes)
        enrollment.setTest1Marks(50);
        enrollment.setTest2Marks(50);
        enrollment.setAssignmentMarks(10);
        enrollment.setOaaMarks(10);
        
        // SEE: 15 + 3 Grace = 18 (Passes SEE because total SEE >= 18)
        enrollment.setSeeMarks(15);
        enrollment.setGraceMarks(3);

        enrollment.calculateGrade();

        assertThat(enrollment.getGrade()).isNotEqualTo(LetterGrade.F);
        assertThat(enrollment.getTotalMarks()).isEqualTo(68); // 50 + 15 + 3 = 68 -> Grade B+
        assertThat(enrollment.getGrade()).isEqualTo(LetterGrade.B_PLUS);
    }
}
