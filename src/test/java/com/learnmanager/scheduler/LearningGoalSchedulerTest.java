package com.learnmanager.scheduler;

import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.Notification;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.learnmanager.testsupport.TestDataFactory.CURRENT_DATE;
import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

class LearningGoalSchedulerTest extends AbstractIntegrationTest {

  @Autowired private LearningGoalScheduler learningGoalScheduler;

  @Autowired private NotificationRepository notificationRepository;

  @Test
  void createGoalDeadlineRemindersShouldCreateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(3));

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    List<Notification> notifications = findNotifications();

    assertThat(notifications).singleElement().satisfies(notification -> {
      assertThat(notification.getUser().getId()).isEqualTo(testData.user().getId());

      assertThat(notification.getType()).isEqualTo(NotificationType.GOAL_DEADLINE_REMINDER);

      assertThat(notification.getReferenceKey()).isEqualTo(NotificationType.GOAL_DEADLINE_REMINDER + ":" + testData.learningGoal()
                                                                                                                   .getId() + ":" + CURRENT_DATE.plusDays(
          3));

      assertThat(notification.isNotificationRead()).isFalse();
    });
  }

  @Test
  void createGoalDeadlineRemindersShouldCreateNotificationForDeadlineToday() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(CURRENT_DATE);

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).singleElement().satisfies(notification -> {
      assertThat(notification.getType()).isEqualTo(NotificationType.GOAL_DEADLINE_REMINDER);

      assertThat(notification.getReferenceKey()).endsWith(":" + CURRENT_DATE);
    });
  }

  @Test
  void createGoalDeadlineRemindersShouldCreateNotificationAtWindowBoundary() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    int reminderDays = testData.notificationSettings().getGoalDeadlineReminderDays();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(reminderDays));

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).hasSize(1);
  }

  @Test
  void createGoalDeadlineRemindersShouldIgnoreGoalOutsideReminderWindow() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    int reminderDays = testData.notificationSettings().getGoalDeadlineReminderDays();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(reminderDays + 1L));

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createGoalDeadlineRemindersShouldIgnoreGoalWithoutDeadline() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(null);

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createGoalDeadlineRemindersShouldIgnoreCompletedGoal() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(3));
    testData.learningGoal().setStatus(GoalStatus.COMPLETED);

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createGoalDeadlineRemindersShouldIgnoreCancelledGoal() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(3));
    testData.learningGoal().setStatus(GoalStatus.CANCELLED);

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createGoalDeadlineRemindersShouldIgnoreDisabledReminderSetting() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(3));

    testData.notificationSettings().update(
        testData.notificationSettings().isPlannedSessionReminderEnabled(),
        testData.notificationSettings().getPlannedSessionReminderMinutes(),
        testData.notificationSettings().isInactivityReminderEnabled(),
        testData.notificationSettings().getInactivityThresholdDays(),
        false,
        testData.notificationSettings().getGoalDeadlineReminderDays(),
        testData.notificationSettings().isPlanDeviationReminderEnabled(),
        testData.notificationSettings().getPlanDeviationThresholdPercent());

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createGoalDeadlineRemindersShouldIgnoreInactiveUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(3));
    testData.user().setActive(false);

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createGoalDeadlineRemindersShouldNotCreateDuplicateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(3));

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();
    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).hasSize(1);
  }

  @Test
  void createGoalDeadlineRemindersShouldCreateNewNotificationAfterDeadlineChange() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long learningGoalId = testData.learningGoal().getId();

    testData.learningGoal().setDeadline(CURRENT_DATE.plusDays(3));

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    LearningGoal learningGoal = entityManager.find(LearningGoal.class, learningGoalId);

    learningGoal.setDeadline(CURRENT_DATE.plusDays(4));

    flushAndClear();

    learningGoalScheduler.createGoalDeadlineReminders();

    flushAndClear();

    assertThat(findNotifications()).hasSize(2).extracting(Notification::getReferenceKey).containsExactlyInAnyOrder(
        NotificationType.GOAL_DEADLINE_REMINDER + ":" + learningGoalId + ":" + CURRENT_DATE.plusDays(3),
        NotificationType.GOAL_DEADLINE_REMINDER + ":" + learningGoalId + ":" + CURRENT_DATE.plusDays(4));
  }

  private List<Notification> findNotifications() {
    return notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);
  }
}