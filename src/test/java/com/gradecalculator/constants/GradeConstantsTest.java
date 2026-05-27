package com.gradecalculator.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for GradeConstants.
 */
class GradeConstantsTest {

    @Test
    @DisplayName("GradeConstants should have private constructor")
    void shouldHavePrivateConstructor() throws Exception {
        Constructor<GradeConstants> constructor = GradeConstants.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Private constructor should throw UnsupportedOperationException")
    void constructorShouldThrowException() throws Exception {
        Constructor<GradeConstants> constructor = GradeConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("All mark range constants should be within valid bounds")
    void markRangesShouldBeValid() {
        assertThat(GradeConstants.MARKS_OUTSTANDING_MIN).isBetween(0, 100);
        assertThat(GradeConstants.MARKS_EXCELLENT_MIN).isBetween(0, 100);
        assertThat(GradeConstants.MARKS_VERY_GOOD_MIN).isBetween(0, 100);
        assertThat(GradeConstants.MARKS_GOOD_MIN).isBetween(0, 100);
        assertThat(GradeConstants.MARKS_ABOVE_AVG_MIN).isBetween(0, 100);
        assertThat(GradeConstants.MARKS_AVERAGE_MIN).isBetween(0, 100);
        assertThat(GradeConstants.MARKS_PASS_MIN).isBetween(0, 100);
        assertThat(GradeConstants.MARKS_FAIL_MAX).isBetween(0, 100);
    }

    @Test
    @DisplayName("Mark ranges should be in descending order")
    void markRangesShouldBeInDescendingOrder() {
        assertThat(GradeConstants.MARKS_OUTSTANDING_MIN)
                .isGreaterThan(GradeConstants.MARKS_EXCELLENT_MIN);
        assertThat(GradeConstants.MARKS_EXCELLENT_MIN)
                .isGreaterThan(GradeConstants.MARKS_VERY_GOOD_MIN);
        assertThat(GradeConstants.MARKS_VERY_GOOD_MIN)
                .isGreaterThan(GradeConstants.MARKS_GOOD_MIN);
        assertThat(GradeConstants.MARKS_GOOD_MIN)
                .isGreaterThan(GradeConstants.MARKS_ABOVE_AVG_MIN);
        assertThat(GradeConstants.MARKS_ABOVE_AVG_MIN)
                .isGreaterThan(GradeConstants.MARKS_AVERAGE_MIN);
        assertThat(GradeConstants.MARKS_AVERAGE_MIN)
                .isGreaterThan(GradeConstants.MARKS_PASS_MIN);
        assertThat(GradeConstants.MARKS_PASS_MIN)
                .isGreaterThan(GradeConstants.MARKS_FAIL_MAX);
    }

    @Test
    @DisplayName("Grade points should be in descending order")
    void gradePointsShouldBeInDescendingOrder() {
        assertThat(GradeConstants.POINTS_OUTSTANDING)
                .isGreaterThan(GradeConstants.POINTS_EXCELLENT);
        assertThat(GradeConstants.POINTS_EXCELLENT)
                .isGreaterThan(GradeConstants.POINTS_VERY_GOOD);
        assertThat(GradeConstants.POINTS_VERY_GOOD)
                .isGreaterThan(GradeConstants.POINTS_GOOD);
        assertThat(GradeConstants.POINTS_GOOD)
                .isGreaterThan(GradeConstants.POINTS_ABOVE_AVG);
        assertThat(GradeConstants.POINTS_ABOVE_AVG)
                .isGreaterThan(GradeConstants.POINTS_AVERAGE);
        assertThat(GradeConstants.POINTS_AVERAGE)
                .isGreaterThan(GradeConstants.POINTS_PASS);
        assertThat(GradeConstants.POINTS_PASS)
                .isGreaterThan(GradeConstants.POINTS_FAIL);
    }

    @Test
    @DisplayName("O grade points should equal 10")
    void outstandingShouldBeTenPoints() {
        assertThat(GradeConstants.POINTS_OUTSTANDING).isEqualTo(10);
    }

    @Test
    @DisplayName("F grade points should be zero")
    void failShouldBeZeroPoints() {
        assertThat(GradeConstants.POINTS_FAIL).isEqualTo(0);
    }

    @Test
    @DisplayName("CIE pass thresholds should be positive")
    void ciePassThresholdsShouldBePositive() {
        assertThat(GradeConstants.CIE_THEORY_PASS_MIN).isPositive();
        assertThat(GradeConstants.CIE_LAB_PASS_MIN).isPositive();
    }

    @Test
    @DisplayName("Lab CIE threshold should be higher than theory")
    void labCieThresholdShouldBeHigherThanTheory() {
        assertThat(GradeConstants.CIE_LAB_PASS_MIN)
                .isGreaterThan(GradeConstants.CIE_THEORY_PASS_MIN);
    }

    @Test
    @DisplayName("SEE pass minimum should be positive and less than max")
    void seePassMinimumShouldBeValid() {
        assertThat(GradeConstants.SEE_PASS_MIN).isPositive();
        assertThat(GradeConstants.SEE_PASS_MIN)
                .isLessThan(GradeConstants.SEE_MAX);
    }

    @Test
    @DisplayName("Total marks pass minimum should be reasonable")
    void totalMarksPassMinimumShouldBeReasonable() {
        assertThat(GradeConstants.TOTAL_MARKS_PASS_MIN).isBetween(30, 50);
        assertThat(GradeConstants.TOTAL_MARKS_MAX).isEqualTo(100);
    }

    @Test
    @DisplayName("CIE weights should sum to 1.0")
    void cieWeightsShouldSumToOne() {
        double sum = GradeConstants.CIE_THEORY_WEIGHT + GradeConstants.CIE_LAB_WEIGHT;
        assertThat(sum).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    @DisplayName("Integrated CIE portions should sum to 1.0")
    void integratedCiePortionsShouldSumToOne() {
        double sum = GradeConstants.INTEGRATED_THEORY_CIE_PORTION + 
                     GradeConstants.INTEGRATED_LAB_CIE_PORTION;
        assertThat(sum).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    @DisplayName("Default credits should be positive")
    void defaultCreditsShouldBePositive() {
        assertThat(GradeConstants.DEFAULT_CREDITS).isPositive();
    }

    @Test
    @DisplayName("Decimal precision should be non-negative")
    void decimalPrecisionShouldBeNonNegative() {
        assertThat(GradeConstants.GRADE_DECIMAL_PRECISION).isGreaterThanOrEqualTo(0);
        assertThat(GradeConstants.GRADE_DECIMAL_PRECISION).isLessThanOrEqualTo(10);
    }
}