package com.pragma.powerup.domain.exception;

public class OrderNotFromRestaurantException extends RuntimeException {
    public OrderNotFromRestaurantException(String message) {
        super(message);
    }
}
