package com.pragma.powerup.domain.exception;

public class NotRestaurantOwnerException extends RuntimeException {
    public NotRestaurantOwnerException(String message) {
        super(message);
    }
}
