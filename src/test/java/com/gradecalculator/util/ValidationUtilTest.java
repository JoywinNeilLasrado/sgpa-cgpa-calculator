package com.gradecalculator.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ValidationUtil.
 */
class ValidationUtilTest {

    @Test
    void requireNonNullPassesWithValidObject() {
        ValidationUtil.requireNonNull("valid", "Should not throw");
        ValidationUtil.requireNonNull(123, "Should not throw");
        ValidationUtil.requireNonNull(new Object(), "Should not throw");
    }

    @Test
    void requireNonNullThrowsWithNullObject() {
        assertThatThrownBy(() -> ValidationUtil.requireNonNull(null, "Object is null"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Object is null");
    }

    @Test
    void requireNonBlankPassesWithValidString() {
        ValidationUtil.requireNonBlank("valid", "Should not throw");
        ValidationUtil.requireNonBlank("  valid with spaces  ", "Should not throw");
    }

    @Test
    void requireNonBlankThrowsWithNullString() {
        assertThatThrownBy(() -> ValidationUtil.requireNonBlank(null, "String is null"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("String is null");
    }

    @Test
    void requireNonBlankThrowsWithEmptyString() {
        assertThatThrownBy(() -> ValidationUtil.requireNonBlank("", "String is empty"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("String is empty");
    }

    @Test
    void requireNonBlankThrowsWithBlankString() {
        assertThatThrownBy(() -> ValidationUtil.requireNonBlank("   ", "String is blank"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("String is blank");
    }

    @Test
    void requireNonBlankTrimsBeforeChecking() {
        // "  " after trim is empty, should fail
        assertThatThrownBy(() -> ValidationUtil.requireNonBlank("  ", "Should fail after trim"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void utilityClassHasPrivateConstructor() throws Exception {
        var constructor = ValidationUtil.class.getDeclaredConstructor();
        assertThat(constructor.isPrivate()).isTrue();
    }
}
