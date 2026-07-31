package com.learnmanager.service;

import com.learnmanager.dto.response.NotificationResponse;
import com.learnmanager.entity.Notification;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  private final HelperService helperService;

  @Transactional(readOnly = true)
  public List<NotificationResponse> getAll(String userEmail) {
    return notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(helperService.normalizeEmail(userEmail))
                                 .stream()
                                 .map(NotificationResponse::fromEntity)
                                 .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getUnread(String userEmail) {
    return notificationRepository.findAllByUser_EmailIgnoreCaseAndNotificationReadFalseOrderByCreatedAtDesc(helperService.normalizeEmail(
                                     userEmail))
                                 .stream()
                                 .map(NotificationResponse::fromEntity)
                                 .toList();
  }

  @Transactional(readOnly = true)
  public NotificationResponse getById(String userEmail, Long notificationId) {
    return NotificationResponse.fromEntity(findOwnedNotification(userEmail, notificationId));
  }

  @Transactional
  public NotificationResponse markAsRead(String userEmail, Long notificationId) {
    Notification notification = findOwnedNotification(userEmail, notificationId);

    notification.setNotificationRead(true);

    return NotificationResponse.fromEntity(notificationRepository.save(notification));
  }

  @Transactional
  public void markAllAsRead(String userEmail) {
    List<Notification> unreadNotifications = notificationRepository.findAllByUser_EmailIgnoreCaseAndNotificationReadFalseOrderByCreatedAtDesc(
        helperService.normalizeEmail(userEmail));

    unreadNotifications.forEach(n -> n.setNotificationRead(true));

    notificationRepository.saveAll(unreadNotifications);
  }

  private Notification findOwnedNotification(String userEmail, Long notificationId) {
    return notificationRepository.findByIdAndUser_EmailIgnoreCase(notificationId, helperService.normalizeEmail(userEmail))
                                 .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
  }
}