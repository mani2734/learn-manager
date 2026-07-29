package com.learnmanager.exception;

public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException() {
    super("A user with this email already exists");
  }
}