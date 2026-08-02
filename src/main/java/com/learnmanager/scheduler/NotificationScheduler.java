package com.learnmanager.scheduler;

import com.learnmanager.entity.Notification;
import com.learnmanager.entity.NotificationSettings;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.repository.NotificationSettingsRepository;
import com.learnmanager.repository.PlannedStudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.learnmanager.LearnManagerApplication.DATE_TIME_FORMATTER;

@Service
@RequiredArgsConstructor
public class NotificationScheduler {

  private static final String PLANNED_SESSION_REMINDER_TITLE = "Planned study session reminder";

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  private final NotificationRepository notificationRepository;

  private final Clock applicationClock;

  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void createPlannedSessionReminders() {
    LocalDateTime now = LocalDateTime.now(applicationClock);

    notificationSettingsRepository.findAllByPlannedSessionReminderEnabledTrueAndUser_ActiveTrue()
                                  .forEach(notificationSettings -> createPlannedSessionReminders(notificationSettings, now));
  }

  private void createPlannedSessionReminders(NotificationSettings notificationSettings, LocalDateTime now) {
    LocalDateTime reminderWindowEnd = now.plusMinutes(notificationSettings.getPlannedSessionReminderMinutes());

    plannedStudySessionRepository.findAllByUser_EmailIgnoreCaseAndStartTimeGreaterThanEqualAndStartTimeLessThanEqualOrderByStartTimeAsc(notificationSettings.getUser().getEmail(),
                                                                                                                                        now,
                                                                                                                                        reminderWindowEnd)
                                 .forEach(this::createPlannedSessionReminder);
  }

  private void createPlannedSessionReminder(
      PlannedStudySession plannedStudySession) {
    String referenceKey = createReferenceKey(plannedStudySession);

    if (notificationRepository.existsByReferenceKey(referenceKey)) {
      return;
    }

    notificationRepository.save(new Notification(
        plannedStudySession.getUser(),
        NotificationType.PLANNED_SESSION_REMINDER,
        PLANNED_SESSION_REMINDER_TITLE,
        createMessage(plannedStudySession),
        referenceKey));
  }

  private String createReferenceKey(PlannedStudySession plannedStudySession) {
    return NotificationType.PLANNED_SESSION_REMINDER + ":" + plannedStudySession.getId() + ":" + plannedStudySession.getStartTime();
  }

  private String createMessage(
      PlannedStudySession plannedStudySession) {
    return "Your planned study session \"" + plannedStudySession.getTitle() + "\" starts on " + plannedStudySession.getStartTime()
                                                                                                                   .format(
                                                                                                                       DATE_TIME_FORMATTER) + ".";
  }
}

