package com.onder.productsearchassistant.controller;

import com.onder.productsearchassistant.exception.ServiceUnavailableException;
import com.onder.productsearchassistant.model.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                           .getFieldErrors()
                           .get(0)
                           .getDefaultMessage();
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .error(message)
                        .status(400)
                        .build()
        );
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                ErrorResponse.builder()
                        .error(ex.getMessage())
                        .status(503)
                        .build()
        );
    }
}
