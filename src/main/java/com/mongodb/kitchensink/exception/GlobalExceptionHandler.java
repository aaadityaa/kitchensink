package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.util.Constants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: @Valid - Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );
        return build(HttpStatus.BAD_REQUEST, Constants.VALIDATION_FAILED, fieldErrors);
    }

    // 400: Malformed body
    @ExceptionHandler({ HttpMessageNotReadableException.class, InvalidFieldException.class })
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : Constants.MALFORMED_REQUEST_BODY_OR_INVALID_VALUE;
        return build(HttpStatus.BAD_REQUEST, Constants.BAD_REQUEST, msg);
    }

    // 401: Authentication problems
    @ExceptionHandler({ AuthenticationException.class})
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : Constants.MISSING_OR_INVALID_CREDENTIALS;
        return build(HttpStatus.UNAUTHORIZED, Constants.UNAUTHORIZED, msg);
    }

    // 403: Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDenied(AccessDeniedException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : Constants.ACCESS_DENIED;
        return build(HttpStatus.FORBIDDEN, Constants.FORBIDDEN, msg);
    }

    // 404: Not found
    @ExceptionHandler({ NoSuchElementException.class, UserNotFoundException.class })
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : Constants.RESOURCE_NOT_FOUND;
        return build(HttpStatus.NOT_FOUND, Constants.NOT_FOUND, msg);
    }

    // 409: Conflicts
    @ExceptionHandler({ DataIntegrityViolationException.class,UserCreationException.class, UserDeletionException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : Constants.RESOURCE_ALREADY_EXISTS_OR_VIOLATES_CONSTRAINTS;
        return build(HttpStatus.CONFLICT, Constants.CONFLICT, msg);
    }

    // 500: Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : Constants.SOMETHING_WENT_WRONG;
        return build(HttpStatus.INTERNAL_SERVER_ERROR, Constants.INTERNAL_SERVER_ERROR, msg);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String error, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.STATUS, status.value());
        body.put(Constants.ERROR, error);
        body.put(Constants.DETAILS, details);
        return ResponseEntity.status(status).body(body);
    }
}

