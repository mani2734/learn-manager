package com.learnmanager.scheduler;

import com.learnmanager.entity.Notification;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.learnmanager.testsupport.TestDataFactory.CURRENT_DATE_TIME;
import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

class InactivitySchedulerTest extends AbstractIntegrationTest {

  @Autowired private InactivityScheduler inactivityScheduler;

  @Autowired private NotificationRepository notificationRepository;

  @Test
  void createInactivityRemindersShouldCreateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.studyTime().setStartTime(CURRENT_DATE_TIME.minusDays(4).minusHours(1));

    testData.studyTime().setEndTime(CURRENT_DATE_TIME.minusDays(4));

    flushAndClear();

    inactivityScheduler.createInactivityReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).singleElement().satisfies(notification -> {
      assertThat(notification.getUser().getId()).isEqualTo(testData.user().getId());

      assertThat(notification.getType()).isEqualTo(NotificationType.INACTIVITY_REMINDER);

      assertThat(notification.getReferenceKey()).startsWith(NotificationType.INACTIVITY_REMINDER + ":" + testData.user().getId() + ":");

      assertThat(notification.isNotificationRead()).isFalse();
    });
  }

  @Test
  void createInactivityRemindersShouldIgnoreUserBelowThreshold() {
    testDataFactory.createCompleteTestData();

    inactivityScheduler.createInactivityReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).isEmpty();
  }

  @Test
  void createInactivityRemindersShouldNotCreateDuplicateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.studyTime().setStartTime(CURRENT_DATE_TIME.minusDays(4).minusHours(1));

    testData.studyTime().setEndTime(CURRENT_DATE_TIME.minusDays(4));

    flushAndClear();

    inactivityScheduler.createInactivityReminders();
    inactivityScheduler.createInactivityReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).hasSize(1);

    assertThat(notifications.getFirst().getType()).isEqualTo(NotificationType.INACTIVITY_REMINDER);
  }

  @Test
  void createInactivityRemindersShouldIgnoreDisabledReminderSetting() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.studyTime().setStartTime(CURRENT_DATE_TIME.minusDays(4).minusHours(1));

    testData.studyTime().setEndTime(CURRENT_DATE_TIME.minusDays(4));

    testData.notificationSettings().update(
        testData.notificationSettings().isPlannedSessionReminderEnabled(),
        testData.notificationSettings().getPlannedSessionReminderMinutes(),
        false,
        testData.notificationSettings().getInactivityThresholdDays(),
        testData.notificationSettings().isGoalDeadlineReminderEnabled(),
        testData.notificationSettings().getGoalDeadlineReminderDays(),
        testData.notificationSettings().isPlanDeviationReminderEnabled(),
        testData.notificationSettings().getPlanDeviationThresholdPercent());

    flushAndClear();

    inactivityScheduler.createInactivityReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).isEmpty();
  }

  @Test
  void createInactivityRemindersShouldIgnoreInactiveUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.studyTime().setStartTime(CURRENT_DATE_TIME.minusDays(4).minusHours(1));

    testData.studyTime().setEndTime(CURRENT_DATE_TIME.minusDays(4));

    testData.user().setActive(false);

    flushAndClear();

    inactivityScheduler.createInactivityReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).isEmpty();
  }

  @Test
  void createInactivityRemindersShouldUseLatestStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.studyTime().setStartTime(CURRENT_DATE_TIME.minusDays(5).minusHours(1));

    testData.studyTime().setEndTime(CURRENT_DATE_TIME.minusDays(5));

    testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        null,
        CURRENT_DATE_TIME.minusHours(2),
        CURRENT_DATE_TIME.minusHours(1));

    flushAndClear();

    inactivityScheduler.createInactivityReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).isEmpty();
  }
}