package com.learnmanager.controller;

import com.learnmanager.dto.request.auth.LoginRequest;
import com.learnmanager.dto.request.auth.RegisterRequest;
import com.learnmanager.dto.response.auth.AuthenticationResponse;
import com.learnmanager.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthenticationResponse register(
      @Valid @RequestBody RegisterRequest request) {
    return authenticationService.register(request);
  }

  @PostMapping("/login")
  public AuthenticationResponse login(
      @Valid @RequestBody LoginRequest request) {
    return authenticationService.login(request);
  }

  @GetMapping("/me")
  public Map<String, Object> getCurrentUser(
      Authentication authentication) {
    List<String> authorities = authentication.getAuthorities().stream().map(Object::toString).toList();

    return Map.of("email", authentication.getName(), "authorities", authorities);
  }
}