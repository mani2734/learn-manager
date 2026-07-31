package com.learnmanager.dto.response;

import com.learnmanager.entity.NotificationSettings;

import java.time.LocalDateTime;

public record NotificationSettingsResponse(Long id, boolean plannedSessionReminderEnabled, Integer plannedSessionReminderMinutes,
                                           boolean inactivityReminderEnabled, Integer inactivityThresholdDays,
                                           boolean goalDeadlineReminderEnabled, Integer goalDeadlineReminderDays,
                                           boolean planDeviationReminderEnabled, Integer planDeviationThresholdPercent,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static NotificationSettingsResponse fromEntity(NotificationSettings notificationSettings) {
    return new NotificationSettingsResponse(
        notificationSettings.getId(),
        notificationSettings.isPlannedSessionReminderEnabled(),
        notificationSettings.getPlannedSessionReminderMinutes(),
        notificationSettings.isInactivityReminderEnabled(),
        notificationSettings.getInactivityThresholdDays(),
        notificationSettings.isGoalDeadlineReminderEnabled(),
        notificationSettings.getGoalDeadlineReminderDays(),
        notificationSettings.isPlanDeviationReminderEnabled(),
        notificationSettings.getPlanDeviationThresholdPercent(),
        notificationSettings.getCreatedAt(),
        notificationSettings.getUpdatedAt());
  }
}