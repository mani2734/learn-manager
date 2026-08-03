package com.learnmanager.scheduler;

import com.learnmanager.entity.Notification;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.learnmanager.testsupport.TestDataFactory.CURRENT_DATE_TIME;
import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationCleanupSchedulerTest extends AbstractIntegrationTest {

  @Autowired private CleanupScheduler cleanupScheduler;

  @Autowired private NotificationRepository notificationRepository;

  @Test
  void deleteOldReadNotificationsShouldDeleteOldReadNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Notification notification = testDataFactory.createNotification(
        testData.user(),
        null,
        "Old read notification",
        "Old notification message",
        "OLD_READ_NOTIFICATION:" + testData.user().getId(),
        true);

    setUpdatedAt(notification, CURRENT_DATE_TIME.minusDays(31));

    flushAndClear();

    cleanupScheduler.deleteOldNotifications();

    flushAndClear();

    assertThat(findNotifications()).isEmpty();
  }

  @Test
  void deleteOldReadNotificationsShouldKeepRecentReadNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Notification notification = testDataFactory.createNotification(
        testData.user(),
        null,
        "Recent read notification",
        "Recent notification message",
        "RECENT_READ_NOTIFICATION:" + testData.user().getId(),
        true);

    setUpdatedAt(notification, CURRENT_DATE_TIME.minusDays(29));

    flushAndClear();

    cleanupScheduler.deleteOldNotifications();

    flushAndClear();

    assertThat(findNotifications()).singleElement().satisfies(savedNotification -> {
      assertThat(savedNotification.getId()).isEqualTo(notification.getId());

      assertThat(savedNotification.isNotificationRead()).isTrue();
    });
  }

  @Test
  void deleteOldReadNotificationsShouldKeepOldUnreadNotification() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Notification notification = testDataFactory.createNotification(
        testData.user(),
        null,
        "Old unread notification",
        "Old notification message",
        "OLD_UNREAD_NOTIFICATION:" + testData.user().getId(),
        false);

    setUpdatedAt(notification, CURRENT_DATE_TIME.minusDays(31));

    flushAndClear();

    cleanupScheduler.deleteOldNotifications();

    flushAndClear();

    assertThat(findNotifications()).singleElement().satisfies(savedNotification -> {
      assertThat(savedNotification.getId()).isEqualTo(notification.getId());

      assertThat(savedNotification.isNotificationRead()).isFalse();
    });
  }

  @Test
  void deleteOldReadNotificationsShouldDeleteOnlyExpiredNotifications() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Notification oldReadNotification = testDataFactory.createNotification(
        testData.user(),
        null,
        "Old read notification",
        "Old notification message",
        "OLD_READ_NOTIFICATION:" + testData.user().getId(),
        true);

    Notification recentReadNotification = testDataFactory.createNotification(
        testData.user(),
        null,
        "Recent read notification",
        "Recent notification message",
        "RECENT_READ_NOTIFICATION:" + testData.user().getId(),
        true);

    Notification oldUnreadNotification = testDataFactory.createNotification(
        testData.user(),
        null,
        "Old unread notification",
        "Old unread notification message",
        "OLD_UNREAD_NOTIFICATION:" + testData.user().getId(),
        false);

    setUpdatedAt(oldReadNotification, CURRENT_DATE_TIME.minusDays(31));

    setUpdatedAt(recentReadNotification, CURRENT_DATE_TIME.minusDays(29));

    setUpdatedAt(oldUnreadNotification, CURRENT_DATE_TIME.minusDays(31));

    flushAndClear();

    cleanupScheduler.deleteOldNotifications();

    flushAndClear();

    assertThat(findNotifications()).hasSize(2)
                                   .extracting(Notification::getReferenceKey)
                                   .containsExactlyInAnyOrder(
                                       recentReadNotification.getReferenceKey(),
                                       oldUnreadNotification.getReferenceKey());
  }

  @Test
  void deleteOldReadNotificationsShouldKeepNotificationAtRetentionBoundary() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Notification notification = testDataFactory.createNotification(
        testData.user(),
        null,
        "Boundary notification",
        "Boundary notification message",
        "BOUNDARY_NOTIFICATION:" + testData.user().getId(),
        true);

    setUpdatedAt(notification, CURRENT_DATE_TIME.minusDays(30));

    flushAndClear();

    cleanupScheduler.deleteOldNotifications();

    flushAndClear();

    assertThat(findNotifications()).hasSize(1);
  }

  private void setUpdatedAt(Notification notification, LocalDateTime updatedAt) {

    ReflectionTestUtils.setField(notification, "updatedAt", updatedAt);
  }

  private List<Notification> findNotifications() {
    return notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL);
  }
}