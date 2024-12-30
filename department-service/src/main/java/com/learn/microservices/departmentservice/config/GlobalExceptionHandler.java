package com.learn.microservices.departmentservice.config;

import com.learn.microservices.departmentservice.dto.ErrorResponse;
import com.learn.microservices.departmentservice.exception.DepartmentAlreadyExistException;
import com.learn.microservices.departmentservice.exception.InvalidPaginationException;
import com.learn.microservices.departmentservice.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Resource not found: {}", ex.getMessage());
        log.trace("Stack trace: ", ex);
        return ResponseEntity.status(status.value())
                .body(new ErrorResponse("Resource not found", ex.getMessage(), status.getReasonPhrase()));
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaginationException(InvalidPaginationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Invalid pagination parameters: {}", ex.getMessage());
        log.trace("Stack trace: ", ex);
        return ResponseEntity.status(status.value())
                .body(new ErrorResponse("Invalid pagination parameters", ex.getMessage(), status.getReasonPhrase()));
    }

    @ExceptionHandler(DepartmentAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentAlreadyExist(DepartmentAlreadyExistException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        log.warn("Department already exist: {}", ex.getMessage());
        log.trace("Stack trace: ", ex);
        return ResponseEntity.status(status.value())
                .body(new ErrorResponse("Department already exist", ex.getMessage(), status.getReasonPhrase()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation failed: {}", errors);
        log.trace("Stack trace: ", ex);

        ErrorResponse errorResponse = new ErrorResponse("Validation failed", errors.toString(), "BAD REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Internal server error: {}", ex.getMessage());
        log.trace("Stack trace: ", ex);
        return ResponseEntity.status(status.value())
                .body(new ErrorResponse("Internal server error", ex.getMessage(), status.getReasonPhrase()));
    }
}
