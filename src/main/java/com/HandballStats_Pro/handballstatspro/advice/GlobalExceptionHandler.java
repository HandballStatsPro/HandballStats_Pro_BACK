package com.HandballStats_Pro.handballstatspro.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.HandballStats_Pro.handballstatspro.dto.ErrorResponse;
import com.HandballStats_Pro.handballstatspro.exceptions.ApiException;
import com.HandballStats_Pro.handballstatspro.exceptions.DuplicateResourceException;
import com.HandballStats_Pro.handballstatspro.exceptions.InvalidCredentialsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(
                        ex.getStatus(),
                        ex.getErrorCode(),
                        ex.getMessage(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validación fallida");

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "validation_error",
                        errorMessage,
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "internal_error",
                        "Ocurrió un error inesperado",
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(
                ex.getStatus(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
                ));
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
            
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                    HttpStatus.CONFLICT,
                    "email_existente",  
                    ex.getMessage(),     
                    request.getDescription(false).replace("uri=", "")
                ));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "invalid_argument",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
                ));
        }
}