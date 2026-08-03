package com.learnmanager.service;

import com.learnmanager.dto.request.update.UpdateNotificationSettingsRequest;
import com.learnmanager.entity.NotificationSettings;
import com.learnmanager.entity.User;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.NotificationSettingsRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationSettingsServiceTest extends AbstractIntegrationTest {

  @Autowired private NotificationSettingsService notificationSettingsService;

  @Autowired private NotificationSettingsRepository notificationSettingsRepository;

  @Test
  void getShouldReturnOwnedNotificationSettings() {
    User user = testDataFactory.createUser();
    testDataFactory.createNotificationSettings(user);

    flushAndClear();

    assertThat(notificationSettingsService.get(TEST_USER_EMAIL.toUpperCase())).isNotNull();
  }

  @Test
  void getShouldRejectMissingNotificationSettings() {
    testDataFactory.createUser();

    flushAndClear();

    assertThatThrownBy(() -> notificationSettingsService.get(TEST_USER_EMAIL)).isInstanceOf(ResourceNotFoundException.class)
                                                                              .hasMessage("Notification settings not found");
  }

  @Test
  void updateShouldUpdateOwnedNotificationSettings() {
    User user = testDataFactory.createUser();
    NotificationSettings notificationSettings = testDataFactory.createNotificationSettings(user);

    UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest(false, 15, false, 5, false, 10, false, 30);

    notificationSettingsService.update(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    NotificationSettings updatedSettings = entityManager.find(NotificationSettings.class, notificationSettings.getId());

    assertThat(updatedSettings.isPlannedSessionReminderEnabled()).isFalse();
    assertThat(updatedSettings.getPlannedSessionReminderMinutes()).isEqualTo(15);
    assertThat(updatedSettings.isInactivityReminderEnabled()).isFalse();
    assertThat(updatedSettings.getInactivityThresholdDays()).isEqualTo(5);
    assertThat(updatedSettings.isGoalDeadlineReminderEnabled()).isFalse();
    assertThat(updatedSettings.getGoalDeadlineReminderDays()).isEqualTo(10);
    assertThat(updatedSettings.isPlanDeviationReminderEnabled()).isFalse();
    assertThat(updatedSettings.getPlanDeviationThresholdPercent()).isEqualTo(30);
  }

  @Test
  void updateShouldOnlyUpdateRequestedUsersSettings() {
    User user = testDataFactory.createUser();
    testDataFactory.createNotificationSettings(user);

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    NotificationSettings otherSettings = testDataFactory.createNotificationSettings(otherUser);

    UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest(false, 15, false, 5, false, 10, false, 30);

    notificationSettingsService.update(TEST_USER_EMAIL, request);

    flushAndClear();

    assertThat(notificationSettingsRepository.findByUser_EmailIgnoreCase(TEST_USER_EMAIL)
                                             .orElseThrow()
                                             .isPlannedSessionReminderEnabled()).isFalse();
    assertThat(entityManager.find(NotificationSettings.class, otherSettings.getId()).isPlannedSessionReminderEnabled()).isTrue();
  }
}
