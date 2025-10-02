package com.tomek4861.cryptopositionmanager.exception;

import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private ResponseEntity<StandardResponse<Object>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(StandardResponse.error(message));
    }

    @ExceptionHandler(CalculationException.class)
    public ResponseEntity<StandardResponse<Object>> handleCalculation(CalculationException ex) {
        logger.warn("Calculation error: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + (fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid"))
                .collect(Collectors.joining("; "));
        logger.warn("Validation failed: {}", details);
        return body(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardResponse<Object>> handleConstraint(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        logger.warn("Constraint violation: {}", details);
        return body(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<StandardResponse<Object>> handleBadParameters(HandlerMethodValidationException ex) {
        List<? extends MessageSourceResolvable> allErrors = ex.getAllErrors();
        String details = allErrors
                .stream()
                .map(
                        error -> {
                            String field = (error.getCodes() != null && error.getCodes().length > 0) ? error.getCodes()[0] : "unknown";
                            return field + ": " + error.getDefaultMessage();
                        }
                )
                .collect(Collectors.joining(", "));
        logger.warn("Bad parameters violation: {}", details);

        return body(HttpStatus.BAD_REQUEST, details);
    }


    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<StandardResponse<Object>> handleBadRequest(Exception ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, "Invalid request parameters");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardResponse<Object>> handleBadJson(HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, "Malformed JSON or wrong data types");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<StandardResponse<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        logger.warn("Method not allowed: {}", ex.getMethod());
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardResponse<Object>> handleAuth(AuthenticationException ex) {
        logger.warn("Authentication exception: {}", ex.getMessage());
        return body(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return body(HttpStatus.FORBIDDEN, "Forbidden");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<StandardResponse<Object>> handleUserNotFound(UsernameNotFoundException ex) {
        logger.warn("Invalid credentials: {}", ex.getMessage());
        return body(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleOther(Exception ex) {
        logger.error("Unhandled exception", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
