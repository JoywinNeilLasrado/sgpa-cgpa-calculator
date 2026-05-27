package com.gradecalculator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EnrollmentMarks grade calculation logic.
 */
class EnrollmentMarksTest {

    @Test
    void calculateGradeForTheoryCourseWithAPlusMarks() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setCieMarks(42);
        marks.setSeeMarks(43);
        marks.setTotalMarks(85);

        LetterGrade grade = marks.calculateGrade(CourseType.THEORY);

        assertThat(grade).isEqualTo(LetterGrade.A_PLUS);
    }

    @Test
    void calculateGradeForTheoryCourseWithFail() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setCieMarks(15);  // Below 20
        marks.setSeeMarks(15);
        marks.setTotalMarks(30);

        LetterGrade grade = marks.calculateGrade(CourseType.THEORY);

        assertThat(grade).isEqualTo(LetterGrade.F);
    }

    @Test
    void calculateGradeForLaboratoryCoursePassing() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setCieMarks(30);
        marks.setSeeMarks(25);

        LetterGrade grade = marks.calculateGrade(CourseType.LABORATORY);

        assertThat(grade).isEqualTo(LetterGrade.B_PLUS);
    }

    @Test
    void calculateGradeForLaboratoryCourseFailOnLowCie() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setCieMarks(20);  // Below 25 required for lab
        marks.setSeeMarks(25);

        LetterGrade grade = marks.calculateGrade(CourseType.LABORATORY);

        assertThat(grade).isEqualTo(LetterGrade.F);
    }

    @Test
    void calculateGradeForIntegratedCoursePassing() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setTest1Marks(70);
        marks.setTest2Marks(75);
        marks.setAssignmentMarks(85);
        marks.setOaaMarks(80);
        marks.setRegularLabMarks(85);
        marks.setLabTestMarks(80);
        marks.setLabRecordMarks(90);
        marks.setSeeMarks(45);
        marks.setGraceMarks(0);

        LetterGrade grade = marks.calculateGrade(CourseType.INTEGRATED);

        assertThat(grade).isNotEqualTo(LetterGrade.F);
    }

    @Test
    void calculateGradeReturnsNullForNullCourseType() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setTotalMarks(85);

        LetterGrade grade = marks.calculateGrade(null);

        assertThat(grade).isNull();
    }

    @Test
    void calculateGradeWithGraceMarks() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setCieMarks(20);
        marks.setSeeMarks(17);
        marks.setGraceMarks(5);
        marks.setTotalMarks(42);

        LetterGrade grade = marks.calculateGrade(CourseType.THEORY);

        assertThat(grade).isEqualTo(LetterGrade.P);
    }

    @Test
    void calculateGradeOutstandingMarks() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setCieMarks(48);
        marks.setSeeMarks(48);
        marks.setTotalMarks(96);

        LetterGrade grade = marks.calculateGrade(CourseType.THEORY);

        assertThat(grade).isEqualTo(LetterGrade.O);
    }

    @Test
    void calculateGradeAverageMarks() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setCieMarks(30);
        marks.setSeeMarks(25);
        marks.setTotalMarks(55);

        LetterGrade grade = marks.calculateGrade(CourseType.THEORY);

        assertThat(grade).isEqualTo(LetterGrade.C);
    }

    @ParameterizedTest
    @CsvSource({
        "90, 10, O",
        "85, 9, A_PLUS",
        "75, 8, A",
        "65, 7, B_PLUS",
        "57, 6, B",
        "52, 5, C",
        "45, 4, P",
        "35, 0, F"
    })
    void verifyGradeCalculationForVariousMarks(int totalMarks, int expectedPoints, String expectedGrade) {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setTotalMarks(totalMarks);
        marks.setSeeMarks(totalMarks);

        LetterGrade grade = marks.calculateGrade(CourseType.THEORY);

        assertThat(grade.getGradePoints()).isEqualTo(expectedPoints);
        assertThat(grade.getGrade()).isEqualTo(expectedGrade);
    }

    @Test
    void handlesNullMarksGracefully() {
        EnrollmentMarks marks = new EnrollmentMarks();

        LetterGrade grade = marks.calculateGrade(CourseType.THEORY);

        assertThat(grade).isNotNull();
    }

    @Test
    void integratedCoursePassesCieButFailsSee() {
        EnrollmentMarks marks = new EnrollmentMarks();
        marks.setTest1Marks(80);
        marks.setTest2Marks(80);
        marks.setAssignmentMarks(80);
        marks.setOaaMarks(80);
        marks.setRegularLabMarks(80);
        marks.setLabTestMarks(80);
        marks.setLabRecordMarks(80);
        marks.setSeeMarks(10);  // Below 18
        marks.setGraceMarks(0);

        LetterGrade grade = marks.calculateGrade(CourseType.INTEGRATED);

        assertThat(grade).isEqualTo(LetterGrade.F);
    }
}
