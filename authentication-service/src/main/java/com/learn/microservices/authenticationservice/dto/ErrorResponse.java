package com.learn.microservices.authenticationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String details;
    private String status;
    private String timestamp;
    

    public ErrorResponse(String message, String details, String status) {
        this.message = message;
        this.details = details;
        this.status = status;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
