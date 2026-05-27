package com.gradecalculator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LetterGrade enum.
 */
class LetterGradeTest {

    @ParameterizedTest
    @CsvSource({
        "100, O",
        "95, O",
        "90, O",
        "89, A_PLUS",
        "85, A_PLUS",
        "80, A_PLUS",
        "79, A",
        "75, A",
        "70, A",
        "69, B_PLUS",
        "65, B_PLUS",
        "60, B_PLUS",
        "59, B",
        "57, B",
        "55, B",
        "54, C",
        "52, C",
        "50, C",
        "49, P",
        "45, P",
        "40, P",
        "39, F",
        "20, F",
        "0, F"
    })
    void fromMarksReturnsCorrectGrade(int marks, String expectedGrade) {
        LetterGrade grade = LetterGrade.fromMarks(marks);
        assertThat(grade.name()).isEqualTo(expectedGrade);
    }

    @Test
    void fromMarksBelowZeroThrows() {
        assertThatThrownBy(() -> LetterGrade.fromMarks(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Marks must be between 0 and 100");
    }

    @Test
    void fromMarksAbove100Throws() {
        assertThatThrownBy(() -> LetterGrade.fromMarks(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Marks must be between 0 and 100");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 39, 49, 54, 59, 69, 79, 89, 100})
    void fromMarksBoundaryValuesWork(int marks) {
        LetterGrade grade = LetterGrade.fromMarks(marks);
        assertThat(grade).isNotNull();
    }

    @Test
    void fromGradeReturnsCorrectGrade() {
        assertThat(LetterGrade.fromGrade("O")).isEqualTo(LetterGrade.O);
        assertThat(LetterGrade.fromGrade("A+")).isEqualTo(LetterGrade.A_PLUS);
        assertThat(LetterGrade.fromGrade("A")).isEqualTo(LetterGrade.A);
        assertThat(LetterGrade.fromGrade("B+")).isEqualTo(LetterGrade.B_PLUS);
        assertThat(LetterGrade.fromGrade("B")).isEqualTo(LetterGrade.B);
        assertThat(LetterGrade.fromGrade("C")).isEqualTo(LetterGrade.C);
        assertThat(LetterGrade.fromGrade("P")).isEqualTo(LetterGrade.P);
        assertThat(LetterGrade.fromGrade("F")).isEqualTo(LetterGrade.F);
    }

    @Test
    void fromGradeIsCaseInsensitive() {
        assertThat(LetterGrade.fromGrade("o")).isEqualTo(LetterGrade.O);
        assertThat(LetterGrade.fromGrade("a+")).isEqualTo(LetterGrade.A_PLUS);
        assertThat(LetterGrade.fromGrade("A+")).isEqualTo(LetterGrade.A_PLUS);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void fromGradeReturnsNullForNullOrEmpty(String grade) {
        assertThat(LetterGrade.fromGrade(grade)).isNull();
    }

    @Test
    void fromGradeThrowsForInvalidGrade() {
        assertThatThrownBy(() -> LetterGrade.fromGrade("X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid grade: X");
    }

    @Test
    void getGradeReturnsCorrectString() {
        assertThat(LetterGrade.O.getGrade()).isEqualTo("O");
        assertThat(LetterGrade.A_PLUS.getGrade()).isEqualTo("A+");
        assertThat(LetterGrade.F.getGrade()).isEqualTo("F");
    }

    @Test
    void getPerformanceLevelReturnsCorrectString() {
        assertThat(LetterGrade.O.getPerformanceLevel()).isEqualTo("Outstanding");
        assertThat(LetterGrade.A_PLUS.getPerformanceLevel()).isEqualTo("Excellent");
        assertThat(LetterGrade.F.getPerformanceLevel()).isEqualTo("Fail");
    }

    @Test
    void getGradePointsReturnsCorrectValues() {
        assertThat(LetterGrade.O.getGradePoints()).isEqualTo(10);
        assertThat(LetterGrade.A_PLUS.getGradePoints()).isEqualTo(9);
        assertThat(LetterGrade.A.getGradePoints()).isEqualTo(8);
        assertThat(LetterGrade.B_PLUS.getGradePoints()).isEqualTo(7);
        assertThat(LetterGrade.B.getGradePoints()).isEqualTo(6);
        assertThat(LetterGrade.C.getGradePoints()).isEqualTo(5);
        assertThat(LetterGrade.P.getGradePoints()).isEqualTo(4);
        assertThat(LetterGrade.F.getGradePoints()).isEqualTo(0);
    }

    @Test
    void allLetterGradesAreUnique() {
        long uniqueGrades = java.util.Arrays.stream(LetterGrade.values())
                .map(LetterGrade::getGrade)
                .distinct()
                .count();
        
        assertThat(uniqueGrades).isEqualTo(LetterGrade.values().length);
    }

    @Test
    void gradePointsAreOrderedCorrectly() {
        int previousPoints = Integer.MAX_VALUE;
        for (LetterGrade grade : LetterGrade.values()) {
            assertThat(grade.getGradePoints()).isLessThanOrEqualTo(previousPoints);
            previousPoints = grade.getGradePoints();
        }
    }
}
