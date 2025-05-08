package com.HandballStats_Pro.handballstatspro.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    // Constructor para ID
    public ResourceNotFoundException(String resource, Long id) {
        super(HttpStatus.NOT_FOUND, 
            "resource_not_found",
            String.format("%s con ID %d no encontrado", resource, id));
    }

    // Constructor para campo + valor
    public ResourceNotFoundException(String resource, String field, String value) {
        super(HttpStatus.NOT_FOUND,
            "resource_not_found",
            String.format("%s con %s '%s' no encontrado", resource, field, value));
    }
}