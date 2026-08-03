package com.learnmanager.service;

import com.learnmanager.entity.User;
import com.learnmanager.entity.enums.Role;
import com.learnmanager.repository.UserRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomUserDetailsServiceTest extends AbstractIntegrationTest {

  @Autowired private CustomUserDetailsService customUserDetailsService;

  @Autowired private UserRepository userRepository;

  @Test
  void loadUserByUsernameShouldReturnSpringSecurityUser() {
    User user = testDataFactory.createUser("admin@learnmanager.local", Role.ADMIN, true);

    flushAndClear();

    org.springframework.security.core.userdetails.UserDetails userDetails = customUserDetailsService.loadUserByUsername(
        " admin@learnmanager.local ");

    assertThat(userDetails.getUsername()).isEqualTo(user.getEmail());
    assertThat(userDetails.getPassword()).isEqualTo(user.getPasswordHash());
    assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    assertThat(userDetails.isEnabled()).isTrue();
  }

  @Test
  void loadUserByUsernameShouldDisableInactiveUsers() {
    testDataFactory.createUser("inactive@learnmanager.local", Role.USER, false);

    flushAndClear();

    assertThat(customUserDetailsService.loadUserByUsername("inactive@learnmanager.local").isEnabled()).isFalse();
  }

  @Test
  void loadUserByUsernameShouldRejectUnknownUser() {
    assertThat(userRepository.findByEmailIgnoreCase("missing@learnmanager.local")).isEmpty();

    assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@learnmanager.local")).isInstanceOf(
        UsernameNotFoundException.class).hasMessage("User not found");
  }
}
