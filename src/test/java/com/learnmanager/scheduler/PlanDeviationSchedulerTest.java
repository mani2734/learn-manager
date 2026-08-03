package com.learnmanager.scheduler;

import com.learnmanager.entity.Notification;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

class PlanDeviationSchedulerTest extends AbstractIntegrationTest {

  @Autowired private PlanDeviationScheduler planDeviationScheduler;

  @Autowired private NotificationRepository notificationRepository;

  @Test
  void createPlanDeviationRemindersShouldCreateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    createActualStudyTime(testData, 23);

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    List<Notification> notifications = findNotifications();

    assertThat(notifications).singleElement().satisfies(notification -> {
      assertThat(notification.getUser().getId()).isEqualTo(testData.user().getId());

      assertThat(notification.getType()).isEqualTo(NotificationType.PLAN_DEVIATION_REMINDER);

      assertThat(notification.getReferenceKey()).startsWith(NotificationType.PLAN_DEVIATION_REMINDER + ":");

      assertThat(notification.isNotificationRead()).isFalse();
    });
  }

  @Test
  void createPlanDeviationRemindersShouldIgnoreDeviationBelowThreshold() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    /*
     * Planned time: 30 hours
     * Actual time: 25 hours
     * Deviation: 16.67 percent
     * Configured threshold: 20 percent
     */
    createActualStudyTime(testData, 25);

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createPlanDeviationRemindersShouldIgnoreActualTimeAbovePlan() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    createActualStudyTime(testData, 31);

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createPlanDeviationRemindersShouldIgnoreModulePlanWithoutPlannedHours() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.modulePlan().setPlannedHours(BigDecimal.ZERO);

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createPlanDeviationRemindersShouldIgnoreDisabledReminderSetting() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.notificationSettings().update(
        testData.notificationSettings().isPlannedSessionReminderEnabled(),
        testData.notificationSettings().getPlannedSessionReminderMinutes(),
        testData.notificationSettings().isInactivityReminderEnabled(),
        testData.notificationSettings().getInactivityThresholdDays(),
        testData.notificationSettings().isGoalDeadlineReminderEnabled(),
        testData.notificationSettings().getGoalDeadlineReminderDays(),
        false,
        testData.notificationSettings().getPlanDeviationThresholdPercent());

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createPlanDeviationRemindersShouldIgnoreInactiveUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testData.user().setActive(false);

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void createPlanDeviationRemindersShouldNotCreateDuplicateNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    createActualStudyTime(testData, 23);

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();
    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    List<Notification> notifications = findNotifications();

    assertThat(notifications).hasSize(1);

    assertThat(notifications.getFirst().getType()).isEqualTo(NotificationType.PLAN_DEVIATION_REMINDER);
  }

  @Test
  void createPlanDeviationRemindersShouldSumMultipleStudyTimes() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDateTime periodStart = testData.planningPeriod().getStartDate().atStartOfDay();

    testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        null,
        periodStart,
        periodStart.plusHours(12));

    testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        null,
        periodStart.plusDays(2),
        periodStart.plusDays(2).plusHours(13));

    flushAndClear();

    planDeviationScheduler.createPlanDeviationReminders();

    flushAndClear();

    /*
     * Total actual time is 25 hours.
     * The deviation from 30 planned hours is below 20 percent.
     */
    assertThat(findNotifications()).isEmpty();
  }

  private void createActualStudyTime(CompleteTestData testData, long actualHours) {

    LocalDateTime periodStart = testData.planningPeriod().getStartDate().atStartOfDay();

    testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        null,
        periodStart,
        periodStart.plusHours(actualHours));
  }

  private List<Notification> findNotifications() {
    return notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);
  }
}