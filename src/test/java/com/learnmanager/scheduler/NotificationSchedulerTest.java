package com.learnmanager.scheduler;

import com.learnmanager.entity.Notification;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationSchedulerTest extends AbstractIntegrationTest {

  @Autowired private PlannedSessionScheduler plannedSessionScheduler;

  @Autowired private NotificationRepository notificationRepository;

  @Test
  void createPlannedSessionRemindersShouldCreateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    PlannedStudySession plannedStudySession = testData.plannedStudySession();

    plannedSessionScheduler.createPlannedSessionReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).singleElement().satisfies(notification -> {
      assertThat(notification.getUser().getId()).isEqualTo(testData.user().getId());

      assertThat(notification.getType()).isEqualTo(NotificationType.PLANNED_SESSION_REMINDER);

      assertThat(notification.getTitle()).isEqualTo("Planned study session reminder");

      assertThat(notification.getMessage()).isEqualTo("Your planned study session \"" + plannedStudySession.getTitle() + "\" starts on 03.08.2026 14:20.");

      assertThat(notification.getReferenceKey()).isEqualTo(NotificationType.PLANNED_SESSION_REMINDER + ":" + plannedStudySession.getId() + ":" + plannedStudySession.getStartTime());

      assertThat(notification.isNotificationRead()).isFalse();
    });
  }

  @Test
  void createPlannedSessionRemindersShouldNotCreateDuplicateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    PlannedStudySession plannedStudySession = testData.plannedStudySession();

    plannedSessionScheduler.createPlannedSessionReminders();
    plannedSessionScheduler.createPlannedSessionReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).hasSize(1);

    assertThat(notifications.getFirst()
                            .getReferenceKey()).isEqualTo(NotificationType.PLANNED_SESSION_REMINDER + ":" + plannedStudySession.getId() + ":" + plannedStudySession.getStartTime());
  }

  @Test
  void createPlannedSessionRemindersShouldIgnoreSessionOutsideReminderWindow() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testDataFactory.createPlannedStudySession(
        testData.user(),
        testData.studyModule(),
        "Later study session",
        TestDataFactory.CURRENT_DATE_TIME.plusHours(2),
        TestDataFactory.CURRENT_DATE_TIME.plusHours(3));

    plannedSessionScheduler.createPlannedSessionReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).hasSize(1);

    assertThat(notifications.getFirst().getMessage()).contains(testData.plannedStudySession().getTitle())
                                                     .doesNotContain("Later study session");
  }

  @Test
  void createPlannedSessionRemindersShouldIgnoreDisabledReminderSetting() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.notificationSettings().update(
        false,
        testData.notificationSettings().getPlannedSessionReminderMinutes(),
        testData.notificationSettings().isInactivityReminderEnabled(),
        testData.notificationSettings().getInactivityThresholdDays(),
        testData.notificationSettings().isGoalDeadlineReminderEnabled(),
        testData.notificationSettings().getGoalDeadlineReminderDays(),
        testData.notificationSettings().isPlanDeviationReminderEnabled(),
        testData.notificationSettings().getPlanDeviationThresholdPercent());

    flushAndClear();

    plannedSessionScheduler.createPlannedSessionReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).isEmpty();
  }

  @Test
  void createPlannedSessionRemindersShouldIgnoreInactiveUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.user().setActive(false);

    flushAndClear();

    plannedSessionScheduler.createPlannedSessionReminders();

    flushAndClear();

    List<Notification> notifications = notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);

    assertThat(notifications).isEmpty();
  }
}