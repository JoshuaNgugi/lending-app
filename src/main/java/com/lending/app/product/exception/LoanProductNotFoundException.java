package com.lending.app.product.exception;

public class LoanProductNotFoundException extends RuntimeException {

    public LoanProductNotFoundException(String message) {
        super(message);
    }
}
