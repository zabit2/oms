package com.project.oms.exception;

public class InvalidPaymentValueException extends RuntimeException {

    public InvalidPaymentValueException(String message) {
        super(message);
    }
}

