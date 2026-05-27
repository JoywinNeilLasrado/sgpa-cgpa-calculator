package com.gradecalculator.exception;

/**
 * Base exception for all application-specific exceptions.
 */
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    
    protected BaseException(String message) {
        super(message);
        this.errorCode = deriveErrorCode();
    }
    
    protected BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = deriveErrorCode();
    }
    
    protected BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    private String deriveErrorCode() {
        String className = getClass().getSimpleName();
        return className
            .replace("Exception", "")
            .toUpperCase()
            .replaceAll("([A-Z])", "_$1")
            .replaceFirst("^_", "")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
    }
}
