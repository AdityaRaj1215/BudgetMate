package com.personalfin.server.security.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLockedException(AccountLockedException ex) {
        logger.warn("Account locked: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Account temporarily locked");
        response.put("message", ex.getMessage());
        response.put("remainingAttempts", ex.getRemainingAttempts());
        response.put("lockoutDurationMinutes", ex.getLockoutDurationMinutes());
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        logger.warn("Security exception: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Security violation");
        response.put("message", "The request could not be processed due to security restrictions");
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        logger.warn("Bad credentials: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Authentication failed");
        response.put("message", "Invalid username or password");
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLockedException(LockedException ex) {
        logger.warn("Account locked: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Account locked");
        response.put("message", "Your account has been temporarily locked due to multiple failed login attempts");
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("message", "Invalid input data");
        response.put("errors", errors);
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "File too large");
        response.put("message", "The uploaded file exceeds the maximum allowed size");
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid request");
        response.put("message", ex.getMessage());
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        logger.error("Unexpected error: ", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("message", "An unexpected error occurred. Please try again later.");
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unhandled exception: ", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("message", "An unexpected error occurred. Please try again later.");
        response.put("timestamp", OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
