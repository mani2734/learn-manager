package com.learnmanager.scheduler;

import com.learnmanager.entity.Notification;
import com.learnmanager.entity.NotificationSettings;
import com.learnmanager.entity.StudyTime;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.repository.NotificationSettingsRepository;
import com.learnmanager.repository.StudyTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.learnmanager.LearnManagerApplication.DATE_TIME_FORMATTER;

@Service
@RequiredArgsConstructor
public class InactivityScheduler {

  private static final String INACTIVITY_REMINDER_TITLE = "Inactivity reminder";

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final NotificationRepository notificationRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final Clock applicationClock;

  @Scheduled(cron = "0 0 7 * * *", zone = "Europe/Vienna")
  @Transactional
  public void createInactivityReminders() {
    LocalDateTime now = LocalDateTime.now(applicationClock);

    notificationSettingsRepository.findAllByInactivityReminderEnabledTrueAndUser_ActiveTrue()
                                  .forEach(notificationSettings -> createInactivityReminder(notificationSettings, now));
  }

  private String createInactivityMessage(NotificationSettings notificationSettings, LocalDateTime lastActivityTime) {
    return "You have not recorded any study time for at least " + notificationSettings.getInactivityThresholdDays() + " days. Your last activity was on " + lastActivityTime.format(
        DATE_TIME_FORMATTER) + ".";
  }

  private String createInactivityReferenceKey(NotificationSettings notificationSettings, LocalDateTime lastActivityTime) {
    return NotificationType.INACTIVITY_REMINDER + ":" + notificationSettings.getUser()
                                                                            .getId() + ":" + lastActivityTime + ":" + notificationSettings.getInactivityThresholdDays();
  }

  private void createInactivityReminder(NotificationSettings notificationSettings, LocalDateTime now) {
    LocalDateTime lastActivityTime = studyTimeRepository.findTopByUser_EmailIgnoreCaseOrderByEndTimeDesc(notificationSettings.getUser()
                                                                                                                             .getEmail())
                                                        .map(StudyTime::getEndTime)
                                                        .orElse(notificationSettings.getUser().getCreatedAt());

    if (lastActivityTime.plusDays(notificationSettings.getInactivityThresholdDays()).isAfter(now)) {
      return;
    }

    String referenceKey = createInactivityReferenceKey(notificationSettings, lastActivityTime);

    if (notificationRepository.existsByReferenceKey(referenceKey)) {
      return;
    }

    notificationRepository.save(new Notification(
        notificationSettings.getUser(),
        NotificationType.INACTIVITY_REMINDER,
        INACTIVITY_REMINDER_TITLE,
        createInactivityMessage(notificationSettings, lastActivityTime),
        referenceKey));
  }
}
