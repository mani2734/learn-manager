package com.learnmanager.service;

import com.learnmanager.entity.User;
import com.learnmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmailIgnoreCase(email.trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                                                             .password(user.getPasswordHash())
                                                             .roles(user.getRole().name())
                                                             .disabled(!user.isActive())
                                                             .build();
  }
}