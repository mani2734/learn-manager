package com.learnmanager.service;

import com.learnmanager.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

  private static final String ROLES_CLAIM = "roles";

  private final JwtEncoder jwtEncoder;

  private final JwtProperties jwtProperties;

  public String generateToken(UserDetails userDetails) {
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(jwtProperties.expiration());

    List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    JwtClaimsSet claims = JwtClaimsSet.builder()
                                      .issuer(jwtProperties.issuer())
                                      .issuedAt(issuedAt)
                                      .expiresAt(expiresAt)
                                      .subject(userDetails.getUsername())
                                      .claim(ROLES_CLAIM, roles)
                                      .build();

    JwtEncoderParameters parameters = JwtEncoderParameters.from(claims);

    return jwtEncoder.encode(parameters).getTokenValue();
  }

  public long getExpirationSeconds() {
    return jwtProperties.expiration().toSeconds();
  }
}