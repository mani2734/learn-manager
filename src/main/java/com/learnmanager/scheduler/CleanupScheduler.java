package com.learnmanager.scheduler;

import com.learnmanager.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CleanupScheduler {

  private final NotificationRepository notificationRepository;

  private final int DAYS_TO_KEEP_NOTIFICATIONS = 5;

  @Scheduled(cron = "0 0 7 * * *")
  @Transactional
  public void deleteOldNotifications() {
    notificationRepository.deleteAll(notificationRepository.findAllByNotificationReadTrueAndUpdatedAtAfter(LocalDateTime.now()
                                                                                                                        .minusDays(
                                                                                                                            DAYS_TO_KEEP_NOTIFICATIONS)));
    ;
  }
}
