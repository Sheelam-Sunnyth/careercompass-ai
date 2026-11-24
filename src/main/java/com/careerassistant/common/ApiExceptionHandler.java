package com.careerassistant.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> api(ApiException ex, HttpServletRequest request) {
        return response(ex.getStatus(), ex.getMessage(), request.getRequestURI());
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<Map<String, Object>> validation(Exception ex, HttpServletRequest request) {
        String message = ex instanceof MethodArgumentNotValidException valid
                ? valid.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse("Validation failed")
                : ex.getMessage();
        return response(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, Object>> fileSize(HttpServletRequest request) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "PDF files must be 10 MB or smaller.", request.getRequestURI());
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> denied(HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.", request.getRequestURI());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.", request.getRequestURI());
    }
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now(), "status", status.value(), "message", message, "path", path));
    }
}
