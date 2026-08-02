package com.learnmanager.repository;

import com.learnmanager.entity.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

  Optional<NotificationSettings> findByUser_EmailIgnoreCase(String email);

  List<NotificationSettings> findAllByPlannedSessionReminderEnabledTrueAndUser_ActiveTrue();
}
