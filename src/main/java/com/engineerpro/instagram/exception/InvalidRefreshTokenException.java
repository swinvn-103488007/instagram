package com.engineerpro.instagram.exception;

public class InvalidRefreshTokenException extends RuntimeException {
  public InvalidRefreshTokenException(String message) {
    super(message);
  }
}