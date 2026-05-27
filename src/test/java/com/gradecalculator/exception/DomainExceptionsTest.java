package com.gradecalculator.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for custom domain exceptions.
 */
class DomainExceptionsTest {

    @Nested
    @DisplayName("StudentNotFoundException")
    class StudentNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with Long ID")
        void shouldCreateWithLongId() {
            StudentNotFoundException ex = new StudentNotFoundException(123L);
            
            assertThat(ex.getMessage()).contains("123");
            assertThat(ex.getErrorCode()).isEqualTo("STUDENT_NOT_FOUND");
        }

        @Test
        @DisplayName("Should create exception with String ID")
        void shouldCreateWithStringId() {
            StudentNotFoundException ex = new StudentNotFoundException("CS2024001");
            
            assertThat(ex.getMessage()).contains("CS2024001");
            assertThat(ex.getErrorCode()).isEqualTo("STUDENT_NOT_FOUND");
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateWithMessageAndCause() {
            Throwable cause = new RuntimeException("Original error");
            StudentNotFoundException ex = new StudentNotFoundException("Custom message", cause);
            
            assertThat(ex.getMessage()).isEqualTo("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("CourseNotFoundException")
    class CourseNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with Long ID")
        void shouldCreateWithLongId() {
            CourseNotFoundException ex = new CourseNotFoundException(456L);
            
            assertThat(ex.getMessage()).contains("456");
            assertThat(ex.getErrorCode()).isEqualTo("COURSE_NOT_FOUND");
        }

        @Test
        @DisplayName("Should create exception with course code")
        void shouldCreateWithCourseCode() {
            CourseNotFoundException ex = new CourseNotFoundException("CS301");
            
            assertThat(ex.getMessage()).contains("CS301");
        }
    }

    @Nested
    @DisplayName("SemesterNotFoundException")
    class SemesterNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with Long ID")
        void shouldCreateWithLongId() {
            SemesterNotFoundException ex = new SemesterNotFoundException(1L);
            
            assertThat(ex.getMessage()).contains("1");
            assertThat(ex.getErrorCode()).isEqualTo("SEMESTER_NOT_FOUND");
        }

        @Test
        @DisplayName("Should create exception with semester number")
        void shouldCreateWithSemesterNumber() {
            SemesterNotFoundException ex = new SemesterNotFoundException(3);
            
            assertThat(ex.getMessage()).contains("3");
        }
    }

    @Nested
    @DisplayName("EnrollmentNotFoundException")
    class EnrollmentNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with Long ID")
        void shouldCreateWithLongId() {
            EnrollmentNotFoundException ex = new EnrollmentNotFoundException(789L);
            
            assertThat(ex.getMessage()).contains("789");
            assertThat(ex.getErrorCode()).isEqualTo("ENROLLMENT_NOT_FOUND");
        }

        @Test
        @DisplayName("Should create exception with custom message")
        void shouldCreateWithCustomMessage() {
            EnrollmentNotFoundException ex = new EnrollmentNotFoundException("Student not enrolled");
            
            assertThat(ex.getMessage()).isEqualTo("Student not enrolled");
        }
    }

    @Nested
    @DisplayName("DuplicateEnrollmentException")
    class DuplicateEnrollmentExceptionTests {

        @Test
        @DisplayName("Should create exception with student and course IDs")
        void shouldCreateWithIds() {
            DuplicateEnrollmentException ex = new DuplicateEnrollmentException(1L, 2L);
            
            assertThat(ex.getMessage()).contains("1");
            assertThat(ex.getMessage()).contains("2");
            assertThat(ex.getErrorCode()).isEqualTo("DUPLICATE_ENROLLMENT");
        }
    }

    @Nested
    @DisplayName("UserNotFoundException")
    class UserNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with Long ID")
        void shouldCreateWithLongId() {
            UserNotFoundException ex = new UserNotFoundException(999L);
            
            assertThat(ex.getMessage()).contains("999");
            assertThat(ex.getErrorCode()).isEqualTo("USER_NOT_FOUND");
        }

        @Test
        @DisplayName("Should create exception with username")
        void shouldCreateWithUsername() {
            UserNotFoundException ex = new UserNotFoundException("john_doe");
            
            assertThat(ex.getMessage()).contains("john_doe");
        }
    }

    @Nested
    @DisplayName("BaseException")
    class BaseExceptionTests {

        @Test
        @DisplayName("All domain exceptions should extend BaseException")
        void allExceptionsShouldExtendBaseException() {
            assertThat(new StudentNotFoundException(1L)).isInstanceOf(BaseException.class);
            assertThat(new CourseNotFoundException(1L)).isInstanceOf(BaseException.class);
            assertThat(new SemesterNotFoundException(1L)).isInstanceOf(BaseException.class);
            assertThat(new EnrollmentNotFoundException(1L)).isInstanceOf(BaseException.class);
            assertThat(new DuplicateEnrollmentException(1L, 1L)).isInstanceOf(BaseException.class);
            assertThat(new UserNotFoundException(1L)).isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("All domain exceptions should be RuntimeExceptions")
        void allExceptionsShouldBeRuntimeExceptions() {
            assertThat(new StudentNotFoundException(1L)).isInstanceOf(RuntimeException.class);
            assertThat(new CourseNotFoundException(1L)).isInstanceOf(RuntimeException.class);
            assertThat(new SemesterNotFoundException(1L)).isInstanceOf(RuntimeException.class);
            assertThat(new EnrollmentNotFoundException(1L)).isInstanceOf(RuntimeException.class);
            assertThat(new DuplicateEnrollmentException(1L, 1L)).isInstanceOf(RuntimeException.class);
            assertThat(new UserNotFoundException(1L)).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Error codes should be uppercase and formatted correctly")
        void errorCodesShouldBeFormattedCorrectly() {
            StudentNotFoundException studentEx = new StudentNotFoundException(1L);
            CourseNotFoundException courseEx = new CourseNotFoundException(1L);
            DuplicateEnrollmentException dupEx = new DuplicateEnrollmentException(1L, 1L);
            
            assertThat(studentEx.getErrorCode()).matches("^[A-Z_]+$");
            assertThat(courseEx.getErrorCode()).matches("^[A-Z_]+$");
            assertThat(dupEx.getErrorCode()).matches("^[A-Z_]+$");
        }
    }
}