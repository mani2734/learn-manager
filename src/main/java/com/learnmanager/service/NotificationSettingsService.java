package com.learnmanager.service;

import com.learnmanager.dto.NotificationSettingsResponse;
import com.learnmanager.dto.UpdateNotificationSettingsRequest;
import com.learnmanager.entity.NotificationSettings;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final HelperService helperService;

  @Transactional(readOnly = true)
  public NotificationSettingsResponse get(String userEmail) {
    return NotificationSettingsResponse.fromEntity(findOwnedNotificationSettings(userEmail));
  }

  @Transactional
  public NotificationSettingsResponse update(String userEmail, UpdateNotificationSettingsRequest request) {
    NotificationSettings notificationSettings = findOwnedNotificationSettings(userEmail);

    notificationSettings.update(
        request.plannedSessionReminderEnabled(),
        request.plannedSessionReminderMinutes(),
        request.inactivityReminderEnabled(),
        request.inactivityThresholdDays(),
        request.goalDeadlineReminderEnabled(),
        request.goalDeadlineReminderDays(),
        request.planDeviationReminderEnabled(),
        request.planDeviationThresholdPercent());

    return NotificationSettingsResponse.fromEntity(notificationSettingsRepository.save(notificationSettings));
  }

  private NotificationSettings findOwnedNotificationSettings(String userEmail) {
    return notificationSettingsRepository.findByUser_EmailIgnoreCase(helperService.normalizeEmail(userEmail))
                                         .orElseThrow(() -> new ResourceNotFoundException("Notification settings not found"));
  }
}