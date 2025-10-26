package com.eaglebank.exception;

import com.eaglebank.gen.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handle401() {
        return ResponseEntity.status(401).body(new ErrorResponse().message("Unauthorized"));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handle403() {
        return ResponseEntity.status(403).body(new ErrorResponse().message("Forbidden"));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handle422() {
        return ResponseEntity.status(422).body(new ErrorResponse().message("Insufficient Funds"));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handle404() {
        return ResponseEntity.status(404).body(new ErrorResponse().message("Not Found"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Invalid details supplied");

        List<Map<String, String>> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            Map<String, String> d = new LinkedHashMap<>();
            d.put("field", err.getField());
            d.put("message", err.getDefaultMessage());
            d.put("type", err.getCode()); // e.g., NotBlank, Pattern, Email
            details.add(d);
        });
        body.put("details", details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // (Optional) for @Validated on path/query params
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolations(ConstraintViolationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Invalid details supplied");
        List<Map<String, String>> details = new ArrayList<>();
        ex.getConstraintViolations().forEach(v -> {
            Map<String, String> d = new LinkedHashMap<>();
            d.put("field", String.valueOf(v.getPropertyPath()));
            d.put("message", v.getMessage());
            d.put("type", v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
            details.add(d);
        });
        body.put("details", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
