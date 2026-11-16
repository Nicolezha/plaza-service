package com.pragma.powerup.domain.exception;

public class InvalidDishDataException extends RuntimeException {
    public InvalidDishDataException(String message) {
        super(message);
    }
}
