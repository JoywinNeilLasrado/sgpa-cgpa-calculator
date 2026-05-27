package com.gradecalculator.exception;

import com.gradecalculator.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, jakarta.servlet.http.HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        String errorMessage = String.join(", ", details);
        
        ErrorResponse errorResponse = new ErrorResponse(
                "BAD_REQUEST",
                400,
                errorMessage,
                request.getRequestURI(),
                LocalDateTime.now(),
                details
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "BAD_REQUEST",
                400,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "CONFLICT",
                409,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(409).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "CONFLICT",
                409,
                "This record conflicts with an existing student, course, or enrollment",
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(409).body(errorResponse);
    }

    @ExceptionHandler(com.gradecalculator.exception.RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(com.gradecalculator.exception.RateLimitException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "TOO_MANY_REQUESTS",
                429,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(429).body(errorResponse);
    }

    @ExceptionHandler(com.gradecalculator.exception.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(com.gradecalculator.exception.NotFoundException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "NOT_FOUND",
                404,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(com.gradecalculator.exception.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(com.gradecalculator.exception.ValidationException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "BAD_REQUEST",
                400,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DOMAIN-SPECIFIC EXCEPTIONS (Custom Business Exceptions)
    // ═══════════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudentNotFound(StudentNotFoundException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(),
                404,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFound(CourseNotFoundException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(),
                404,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(SemesterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSemesterNotFound(SemesterNotFoundException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(),
                404,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(),
                404,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(EnrollmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEnrollmentNotFound(EnrollmentNotFoundException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(),
                404,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    @ExceptionHandler(DuplicateEnrollmentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEnrollment(DuplicateEnrollmentException e, jakarta.servlet.http.HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(),
                409,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(409).body(errorResponse);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // BASE EXCEPTION HANDLER (Fallback for all BaseException subclasses)
    // ═══════════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e, jakarta.servlet.http.HttpServletRequest request) {
        int status = determineHttpStatus(e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(),
                status,
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    private int determineHttpStatus(BaseException e) {
        String code = e.getErrorCode();
        if (code.contains("NOT_FOUND")) {
            return 404;
        } else if (code.contains("DUPLICATE") || code.contains("CONFLICT")) {
            return 409;
        } else if (code.contains("VALIDATION") || code.contains("BAD_REQUEST")) {
            return 400;
        } else if (code.contains("UNAUTHORIZED")) {
            return 401;
        } else if (code.contains("FORBIDDEN")) {
            return 403;
        }
        return 400;
    }
}
