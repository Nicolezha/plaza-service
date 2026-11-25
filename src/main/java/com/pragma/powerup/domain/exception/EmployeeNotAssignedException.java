package com.pragma.powerup.domain.exception;

public class EmployeeNotAssignedException extends RuntimeException {
    public EmployeeNotAssignedException(String message) {
        super(message);
    }
}
