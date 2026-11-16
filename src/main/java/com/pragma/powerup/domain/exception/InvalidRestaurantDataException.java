package com.pragma.powerup.domain.exception;

public class InvalidRestaurantDataException extends RuntimeException {
    public InvalidRestaurantDataException(String message) {
        super(message);
    }
}
