package com.learnmanager.scheduler;

import com.learnmanager.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CleanupScheduler {

  private final NotificationRepository notificationRepository;

  private final Clock applicationClock;

  private static final int DAYS_TO_KEEP_NOTIFICATIONS = 30;

  @Scheduled(cron = "0 0 7 * * *")
  @Transactional
  public void deleteOldNotifications() {
    notificationRepository.deleteAll(notificationRepository.findAllByNotificationReadTrueAndUpdatedAtBefore(LocalDateTime.now(
        applicationClock).minusDays(DAYS_TO_KEEP_NOTIFICATIONS)));
  }
}
