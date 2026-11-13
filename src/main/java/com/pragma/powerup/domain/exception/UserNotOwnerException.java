package com.pragma.powerup.domain.exception;

public class UserNotOwnerException extends RuntimeException {
  public UserNotOwnerException(String message) {
    super(message);
  }
}
