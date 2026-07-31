package com.learnmanager.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticationResponse(

    @JsonProperty("access_token") String accessToken,

    @JsonProperty("token_type") String tokenType,

    @JsonProperty("expires_in") long expiresIn) {

  public AuthenticationResponse(String accessToken, long expiresIn) {
    this(accessToken, "Bearer", expiresIn);
  }
}