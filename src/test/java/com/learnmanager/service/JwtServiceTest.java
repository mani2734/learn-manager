package com.learnmanager.service;

import com.learnmanager.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtServiceTest {

  @Test
  void generateTokenShouldEncodeSubjectIssuerExpirationAndRoles() {
    JwtEncoder jwtEncoder = mock(JwtEncoder.class);
    JwtProperties jwtProperties = new JwtProperties("secret", "learn-manager", Duration.ofHours(2));
    JwtService jwtService = new JwtService(jwtEncoder, jwtProperties);
    UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user@learnmanager.local")
                                                                                .password("encoded-password")
                                                                                .roles("USER")
                                                                                .build();

    when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(Jwt.withTokenValue("signed-token")
                                                                                                        .header("alg", "none")
                                                                                                        .claim(
                                                                                                            "sub",
                                                                                                            "user@learnmanager.local")
                                                                                                        .build());

    String token = jwtService.generateToken(userDetails);

    ArgumentCaptor<JwtEncoderParameters> parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

    verify(jwtEncoder).encode(parametersCaptor.capture());

    JwtClaimsSet claims = parametersCaptor.getValue().getClaims();

    assertThat(token).isEqualTo("signed-token");
    assertThat(claims.getSubject()).isEqualTo("user@learnmanager.local");
    assertThat(claims.getClaimAsString(JwtClaimNames.ISS)).isEqualTo("learn-manager");
    assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
    assertThat(claims.getClaimAsStringList("roles")).containsExactly("ROLE_USER");
  }

  @Test
  void getExpirationSecondsShouldReturnConfiguredExpiration() {
    JwtService jwtService = new JwtService(mock(JwtEncoder.class), new JwtProperties("secret", "learn-manager", Duration.ofMinutes(90)));

    assertThat(jwtService.getExpirationSeconds()).isEqualTo(5400);
  }
}
