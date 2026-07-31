package com.learnmanager.dto.response;

import com.learnmanager.entity.Notification;
import com.learnmanager.entity.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, NotificationType type, String title, String message, boolean readStatus,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static NotificationResponse fromEntity(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getMessage(),
        notification.isNotificationRead(),
        notification.getCreatedAt(),
        notification.getUpdatedAt());
  }
}