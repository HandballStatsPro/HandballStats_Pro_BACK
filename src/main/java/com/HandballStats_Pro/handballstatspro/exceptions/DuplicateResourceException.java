package com.HandballStats_Pro.handballstatspro.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {
    public DuplicateResourceException(String resource, String field) {
        super(HttpStatus.CONFLICT,
            "duplicate_resource",
            String.format("%s con %s ya existe", resource, field));
    }

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, "duplicate_resource", message);
    }
}