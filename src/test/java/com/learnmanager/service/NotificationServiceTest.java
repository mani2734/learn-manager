package com.learnmanager.service;

import com.learnmanager.entity.Notification;
import com.learnmanager.entity.User;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationServiceTest extends AbstractIntegrationTest {

  @Autowired private NotificationService notificationService;

  @Test
  void getAllShouldOnlyReturnNotificationsOfRequestedUser() {
    User user = testDataFactory.createUser();
    testDataFactory.createNotification(user);

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    testDataFactory.createNotification(otherUser);

    flushAndClear();

    assertThat(notificationService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(1);
  }

  @Test
  void getUnreadShouldOnlyReturnUnreadNotifications() {
    User user = testDataFactory.createUser();

    testDataFactory.createNotification(user, NotificationType.PLANNED_SESSION_REMINDER, "Unread", "Unread message", "UNREAD", false);
    testDataFactory.createNotification(user, NotificationType.INACTIVITY_REMINDER, "Read", "Read message", "READ", true);

    flushAndClear();

    assertThat(notificationService.getUnread(TEST_USER_EMAIL)).hasSize(1);
  }

  @Test
  void getByIdShouldRejectForeignNotification() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    Notification foreignNotification = testDataFactory.createNotification(otherUser);

    flushAndClear();

    assertThatThrownBy(() -> notificationService.getById(TEST_USER_EMAIL, foreignNotification.getId())).isInstanceOf(
        ResourceNotFoundException.class);
  }

  @Test
  void markAsReadShouldUpdateNotification() {
    User user = testDataFactory.createUser();
    Notification notification = testDataFactory.createNotification(user);

    notificationService.markAsRead(TEST_USER_EMAIL.toUpperCase(), notification.getId());

    flushAndClear();

    assertThat(entityManager.find(Notification.class, notification.getId()).isNotificationRead()).isTrue();
  }

  @Test
  void markAllAsReadShouldUpdateOnlyRequestedUsersNotifications() {
    User user = testDataFactory.createUser();
    Notification notification = testDataFactory.createNotification(user);

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    Notification otherNotification = testDataFactory.createNotification(otherUser);

    notificationService.markAllAsRead(TEST_USER_EMAIL.toUpperCase());

    flushAndClear();

    assertThat(entityManager.find(Notification.class, notification.getId()).isNotificationRead()).isTrue();
    assertThat(entityManager.find(Notification.class, otherNotification.getId()).isNotificationRead()).isFalse();
  }

}
