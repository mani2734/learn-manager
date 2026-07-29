package com.learnmanager.config;

import com.learnmanager.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ProblemDetail handleEmailAlreadyExists(
      EmailAlreadyExistsException exception) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());

    problemDetail.setTitle("Email already exists");

    return problemDetail;
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(
      AuthenticationException exception) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password");

    problemDetail.setTitle("Authentication failed");

    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(
      MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {

      errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request contains invalid values");

    problemDetail.setTitle("Validation failed");
    problemDetail.setProperty("errors", errors);

    return problemDetail;
  }
}