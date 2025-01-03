package com.learn.microservices.authenticationservice.config;

import com.learn.microservices.authenticationservice.dto.ErrorResponse;
import com.learn.microservices.authenticationservice.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Resource not found: {}", ex.getMessage());
        log.trace("Stack trace: ", ex);
        ErrorResponse errorResponse = new ErrorResponse("Resource not found", ex.getMessage(), status.getReasonPhrase());
        return ResponseEntity.status(status.value()).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse errorResponse = new ErrorResponse("Unauthorized Access", ex.getMessage(), status.getReasonPhrase());
        log.warn("Unauthorized Access: {}", ex.getMessage());
        log.trace("Stack trace: ", ex);
        return ResponseEntity.status(status.value()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = new ErrorResponse("General Error", ex.getMessage(), status.getReasonPhrase());
        log.error("Internal Server Error: {}", ex.getMessage());
        log.trace("Stack trace: ", ex);
        return ResponseEntity.status(status.value()).body(errorResponse);
    }
}
