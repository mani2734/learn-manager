package com.learnmanager.service;

import com.learnmanager.dto.request.auth.LoginRequest;
import com.learnmanager.dto.request.auth.RegisterRequest;
import com.learnmanager.dto.response.auth.AuthenticationResponse;
import com.learnmanager.entity.User;
import com.learnmanager.exception.EmailAlreadyExistsException;
import com.learnmanager.repository.NotificationSettingsRepository;
import com.learnmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private NotificationSettingsRepository notificationSettingsRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private CustomUserDetailsService customUserDetailsService;

  @Mock private JwtService jwtService;

  @InjectMocks private AuthenticationService authenticationService;

  @Test
  void registerShouldNormalizeEmailCreateUserSettingsAndToken() {
    UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user@learnmanager.local")
                                                                                .password("encoded-password")
                                                                                .roles("USER")
                                                                                .build();

    when(userRepository.existsByEmailIgnoreCase("user@learnmanager.local")).thenReturn(false);
    when(passwordEncoder.encode("Password2026")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(customUserDetailsService.loadUserByUsername("user@learnmanager.local")).thenReturn(userDetails);
    when(jwtService.generateToken(userDetails)).thenReturn("access-token");
    when(jwtService.getExpirationSeconds()).thenReturn(3600L);

    AuthenticationResponse response = authenticationService.register(new RegisterRequest("  USER@LearnManager.Local  ", "Password2026"));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    verify(userRepository).save(userCaptor.capture());
    verify(notificationSettingsRepository).save(argThat(settings -> settings.getUser() == userCaptor.getValue()));

    assertThat(userCaptor.getValue().getEmail()).isEqualTo("user@learnmanager.local");
    assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresIn()).isEqualTo(3600L);
  }

  @Test
  void registerShouldRejectDuplicateEmail() {
    when(userRepository.existsByEmailIgnoreCase("user@learnmanager.local")).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.register(new RegisterRequest(" USER@LearnManager.Local ", "Password2026"))).isInstanceOf(
        EmailAlreadyExistsException.class);

    verify(userRepository, never()).save(any());
    verify(notificationSettingsRepository, never()).save(any());
  }

  @Test
  void loginShouldAuthenticateWithNormalizedEmailAndReturnToken() {
    UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user@learnmanager.local")
                                                                                .password("encoded-password")
                                                                                .roles("USER")
                                                                                .build();
    Authentication authentication = mock(Authentication.class);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtService.generateToken(userDetails)).thenReturn("access-token");
    when(jwtService.getExpirationSeconds()).thenReturn(3600L);

    AuthenticationResponse response = authenticationService.login(new LoginRequest(" USER@LearnManager.Local ", "Password2026"));

    ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

    verify(authenticationManager).authenticate(tokenCaptor.capture());

    assertThat(tokenCaptor.getValue().getPrincipal()).isEqualTo("user@learnmanager.local");
    assertThat(tokenCaptor.getValue().getCredentials()).isEqualTo("Password2026");
    assertThat(response.accessToken()).isEqualTo("access-token");
  }
}
