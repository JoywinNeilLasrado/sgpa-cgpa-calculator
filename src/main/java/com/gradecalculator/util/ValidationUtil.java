package com.gradecalculator.util;

/**
 * Centralised validation helpers used throughout the application.
 *
 * <p>All methods throw {@link IllegalArgumentException} when the supplied value
 * does not satisfy the required condition. Keeping the exception type unchanged
 * ensures existing error‑handling and tests continue to work.</p>
 */
public final class ValidationUtil {

    private ValidationUtil() { /* utility class */ }

    /**
     * Ensures the supplied object is not {@code null}.
     *
     * @param obj   the object to validate
     * @param message the exception message if {@code obj} is {@code null}
     * @throws IllegalArgumentException if {@code obj} is {@code null}
     */
    public static void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures the supplied {@link CharSequence} is neither {@code null} nor blank.
     *
     * @param cs      the character sequence to validate
     * @param message the exception message if validation fails
     * @throws IllegalArgumentException if {@code cs} is {@code null} or blank
     */
    public static void requireNonBlank(CharSequence cs, String message) {
        if (cs == null || cs.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
