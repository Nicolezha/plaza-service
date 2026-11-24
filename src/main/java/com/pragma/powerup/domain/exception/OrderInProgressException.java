package com.pragma.powerup.domain.exception;

public class OrderInProgressException extends RuntimeException {
    public OrderInProgressException(String message) {
        super(message);
    }
}
