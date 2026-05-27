package com.gradecalculator.exception;

import com.gradecalculator.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleValidationExceptionsReturns400() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "error message");
        
        when(bindingResult.getFieldErrors())
            .thenReturn(List.of(fieldError));
        
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().code()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("field");
    }

    @Test
    void handleBadRequestWithIllegalArgumentExceptionReturns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid input");
    }

    @Test
    void handleConflictWithIllegalStateExceptionReturns409() {
        IllegalStateException ex = new IllegalStateException("Resource already exists");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("CONFLICT");
    }

    @Test
    void handleNotFoundExceptionReturns404() {
        NotFoundException ex = new NotFoundException("Student not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("Student not found");
    }

    @Test
    void handleValidationExceptionReturns400() {
        ValidationException ex = new ValidationException("Invalid grade value");

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid grade value");
    }

    @Test
    void handleRateLimitExceptionReturns429() {
        RateLimitException ex = new RateLimitException("Too many requests");

        ResponseEntity<ErrorResponse> response = handler.handleRateLimit(ex, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("TOO_MANY_REQUESTS");
    }

    @Test
    void errorResponseContainsTimestamp() {
        IllegalArgumentException ex = new IllegalArgumentException("Test error");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, mockRequest);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void errorResponseContainsRequestPath() {
        IllegalArgumentException ex = new IllegalArgumentException("Test error");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, mockRequest);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    void errorResponseContainsDetailsList() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("obj", "f1", "error1");
        FieldError fieldError2 = new FieldError("obj", "f2", "error2");
        
        when(bindingResult.getFieldErrors())
            .thenReturn(List.of(fieldError1, fieldError2));
        
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, mockRequest);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().details()).isNotEmpty();
        assertThat(response.getBody().details()).hasSize(2);
    }
}
