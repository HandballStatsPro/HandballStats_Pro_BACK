package com.HandballStats_Pro.handballstatspro.exceptions;

import org.springframework.http.HttpStatus;

public class PermissionDeniedException extends ApiException {
    public PermissionDeniedException() {
        super(HttpStatus.FORBIDDEN,
              "permission_denied",
              "No tienes permisos para realizar esta acción");
    }
}