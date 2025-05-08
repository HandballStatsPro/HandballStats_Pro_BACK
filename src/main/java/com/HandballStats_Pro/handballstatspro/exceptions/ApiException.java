package com.HandballStats_Pro.handballstatspro.exceptions;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    // Getters
    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
}