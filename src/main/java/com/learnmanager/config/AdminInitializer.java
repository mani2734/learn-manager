package com.learnmanager.config;

import com.learnmanager.entity.NotificationSettings;
import com.learnmanager.entity.User;
import com.learnmanager.entity.enums.Role;
import com.learnmanager.repository.NotificationSettingsRepository;
import com.learnmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

  private static final String ADMIN_EMAIL = "admin@learnmanager";

  private static final String ADMIN_PASSWORD = "Admin2026";

  private final UserRepository userRepository;

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments arguments) {
    if (userRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
      return;
    }

    User admin = new User(ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD), Role.ADMIN);

    User savedAdmin = userRepository.save(admin);

    NotificationSettings notificationSettings = new NotificationSettings(savedAdmin);

    notificationSettingsRepository.save(notificationSettings);

    log.info("Default administrator account created");
  }
}