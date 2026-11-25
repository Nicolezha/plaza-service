package com.pragma.powerup.domain.exception;

public class EmployeeAlreadyAssignedException extends RuntimeException {
    public EmployeeAlreadyAssignedException(String message) {
        super(message);
    }
}
