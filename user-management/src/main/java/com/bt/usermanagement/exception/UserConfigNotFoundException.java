package com.bt.usermanagement.exception;

public class UserConfigNotFoundException extends Exception {

  public UserConfigNotFoundException(final String message) {
    super(message);
  }
/*
  public UserConfigNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UserConfigNotFoundException(final Throwable cause) {
    super(cause);
  }*/
}
