package com.learnmanager.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

  private static final int MINIMUM_SECRET_LENGTH_BYTES = 32;

  @Bean
  public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
    byte[] secretBytes;

    try {
      secretBytes = Base64.getDecoder().decode(jwtProperties.secret());
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException("security.jwt.secret must be a valid Base64 value", exception);
    }

    if (secretBytes.length < MINIMUM_SECRET_LENGTH_BYTES) {
      throw new IllegalStateException("security.jwt.secret must contain at least 32 bytes");
    }

    return new SecretKeySpec(secretBytes, "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
    return NimbusJwtEncoder.withSecretKey(jwtSecretKey).algorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKey jwtSecretKey, JwtProperties jwtProperties) {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();

    jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));

    return jwtDecoder;
  }
}