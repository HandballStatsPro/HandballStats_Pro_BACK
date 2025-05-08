package com.HandballStats_Pro.handballstatspro.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, 
              "invalid_credentials", 
              "Email o contraseña incorrectos");
    }
}