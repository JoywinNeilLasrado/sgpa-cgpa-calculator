package com.gradecalculator.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for all endpoints.
 * <p>
 * Provides consistent response structure across the API:
 * <ul>
 *   <li>{@code success} - indicates if the operation was successful</li>
 *   <li>{@code message} - human-readable status message</li>
 *   <li>{@code data} - the payload (null for void responses)</li>
 *   <li>{@code timestamp} - server-side timestamp of the response</li>
 *   <li>{@code path} - the API path that generated this response</li>
 * </ul>
 *
 * @param <T> the type of data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp,
        String path,
        String errorCode
) {

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data, LocalDateTime.now(), null, null);
    }

    /**
     * Creates a successful response with custom message and data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), null, null);
    }

    /**
     * Creates a successful response with custom message and null data.
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now(), null, null);
    }

    /**
     * Creates a failed response with error message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), null, null);
    }

    /**
     * Creates a failed response with error message and error code.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), null, errorCode);
    }

    /**
     * Creates a failed response with message, error code, and request path.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, String path) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), path, errorCode);
    }

    /**
     * Creates a paginated response wrapper.
     */
    public static <T> ApiResponse<T> paginated(T data, long totalElements, int totalPages) {
        return new ApiResponse<>(
                true,
                String.format("Showing %d results", totalElements),
                data,
                LocalDateTime.now(),
                null,
                "PAGINATED"
        );
    }
}