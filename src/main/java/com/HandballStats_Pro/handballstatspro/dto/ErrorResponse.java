package com.HandballStats_Pro.handballstatspro.dto;

import lombok.Data;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;

    public ErrorResponse(HttpStatus status, String code, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.code = code;
        this.message = message;
        this.path = path;
    }
}