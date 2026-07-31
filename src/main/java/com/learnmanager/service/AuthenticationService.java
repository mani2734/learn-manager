package com.learnmanager.service;

import com.learnmanager.dto.request.auth.LoginRequest;
import com.learnmanager.dto.request.auth.RegisterRequest;
import com.learnmanager.dto.response.auth.AuthenticationResponse;
import com.learnmanager.entity.NotificationSettings;
import com.learnmanager.entity.User;
import com.learnmanager.exception.EmailAlreadyExistsException;
import com.learnmanager.repository.NotificationSettingsRepository;
import com.learnmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private static final String TOKEN_TYPE = "Bearer";

  private final UserRepository userRepository;

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final PasswordEncoder passwordEncoder;

  private final AuthenticationManager authenticationManager;

  private final CustomUserDetailsService customUserDetailsService;

  private final JwtService jwtService;

  @Transactional
  public AuthenticationResponse register(RegisterRequest request) {
    String email = normalizeEmail(request.email());

    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new EmailAlreadyExistsException();
    }

    User user = new User(email, passwordEncoder.encode(request.password()));

    user = userRepository.save(user);

    NotificationSettings notificationSettings = new NotificationSettings(user);

    notificationSettingsRepository.save(notificationSettings);

    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

    return createAuthenticationResponse(userDetails);
  }

  public AuthenticationResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());

    Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(email, request.password());

    Authentication authentication = authenticationManager.authenticate(authenticationRequest);

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    return createAuthenticationResponse(userDetails);
  }

  private AuthenticationResponse createAuthenticationResponse(
      UserDetails userDetails) {
    String accessToken = jwtService.generateToken(userDetails);

    return new AuthenticationResponse(accessToken, TOKEN_TYPE, jwtService.getExpirationSeconds());
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}