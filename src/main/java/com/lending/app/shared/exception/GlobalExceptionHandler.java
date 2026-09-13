package com.lending.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lending.app.customer.exception.CustomerAlreadyExistsException;
import com.lending.app.customer.exception.CustomerNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomerNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("CUSTOMER NOT FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    ResponseEntity<ApiError> handleConflict(CustomerAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("CUSTOMER_ALREADY_EXISTS", ex.getMessage()));
    }
}
