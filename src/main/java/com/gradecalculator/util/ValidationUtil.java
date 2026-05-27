package com.gradecalculator.util;

import com.gradecalculator.exception.ValidationException;

/**
 * Centralised validation helpers used throughout the application.
 *
 * <p>All methods throw {@link ValidationException} when the supplied value
 * does not satisfy the required condition. Since ValidationException inherits
 * from IllegalArgumentException, existing error-handling and tests continue to work.</p>
 */
public final class ValidationUtil {

    private ValidationUtil() { /* utility class */ }

    /**
     * Ensures the supplied object is not {@code null}.
     *
     * @param obj   the object to validate
     * @param message the exception message if {@code obj} is {@code null}
     * @throws ValidationException if {@code obj} is {@code null}
     */
    public static void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new ValidationException(message);
        }
    }

    /**
     * Ensures the supplied {@link CharSequence} is neither {@code null} nor blank.
     *
     * @param cs      the character sequence to validate
     * @param message the exception message if validation fails
     * @throws ValidationException if {@code cs} is {@code null} or blank
     */
    public static void requireNonBlank(CharSequence cs, String message) {
        if (cs == null || cs.toString().trim().isEmpty()) {
            throw new ValidationException(message);
        }
    }
}
