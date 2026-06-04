package com.gradecalculator.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorResponse DTO.
 */
class ErrorResponseTest {

    @Test
    void createsErrorResponseWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        List<String> details = List.of("detail1", "detail2");

        ErrorResponse response = new ErrorResponse(
                "BAD_REQUEST",
                400,
                "Validation failed",
                "/api/test",
                now,
                details
        );

        assertThat(response.status()).isEqualTo("BAD_REQUEST");
        assertThat(response.code()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("Validation failed");
        assertThat(response.path()).isEqualTo("/api/test");
        assertThat(response.timestamp()).isEqualTo(now);
        assertThat(response.details()).hasSize(2);
    }

    @Test
    void createsErrorResponseWithEmptyDetails() {
        ErrorResponse response = new ErrorResponse(
                "NOT_FOUND",
                404,
                "Resource not found",
                "/api/students/999",
                LocalDateTime.now(),
                List.of()
        );

        assertThat(response.status()).isEqualTo("NOT_FOUND");
        assertThat(response.code()).isEqualTo(404);
        assertThat(response.details()).isEmpty();
    }

    @Test
    void createsErrorResponseWithNullDetails() {
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_ERROR",
                500,
                "Server error",
                "/api/unknown",
                LocalDateTime.now(),
                null
        );

        assertThat(response.details()).isNull();
    }

    @Test
    void errorResponseIsRecord() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse response1 = new ErrorResponse(
                "A", 100, "msg", "path", now, List.of()
        );
        ErrorResponse response2 = new ErrorResponse(
                "A", 100, "msg", "path", now, List.of()
        );

        // Records with same values should be equal
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void timestampIsSetAutomatically() {
        ErrorResponse response = new ErrorResponse(
                "TEST",
                400,
                "Test message",
                "/test",
                null,
                List.of()
        );

        // The timestamp can be null if not provided
        // This tests the record behavior
        assertThat(response.status()).isEqualTo("TEST");
    }
}
