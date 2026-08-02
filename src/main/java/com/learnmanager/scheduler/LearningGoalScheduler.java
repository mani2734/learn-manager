package com.learnmanager.scheduler;

import com.learnmanager.LearnManagerApplication;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.Notification;
import com.learnmanager.entity.NotificationSettings;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.LearningGoalRepository;
import com.learnmanager.repository.NotificationRepository;
import com.learnmanager.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LearningGoalScheduler {

  private static final String GOAL_DEADLINE_REMINDER_TITLE = "Learning goal deadline reminder";

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final NotificationRepository notificationRepository;

  private final LearningGoalRepository learningGoalRepository;

  private final Clock applicationClock;

  @Scheduled(cron = "0 5 7 * * *")
  @Transactional
  public void createGoalDeadlineReminders() {
    LocalDate today = LocalDate.now(applicationClock);

    notificationSettingsRepository.findAllByGoalDeadlineReminderEnabledTrueAndUser_ActiveTrue()
                                  .forEach(notificationSettings -> createGoalDeadlineReminders(notificationSettings, today));
  }

  private void createGoalDeadlineReminders(NotificationSettings notificationSettings, LocalDate today) {
    LocalDate reminderWindowEnd = today.plusDays(notificationSettings.getGoalDeadlineReminderDays());

    learningGoalRepository.findAllByStudyModule_User_EmailIgnoreCaseFalseAndDeadlineBetweenOrderByDeadlineAsc(
                              notificationSettings.getUser().getEmail(),
                                                                                                                          today,
                                                                                                                          reminderWindowEnd)
                          .forEach(learningGoal -> createGoalDeadlineReminder(learningGoal, today));
  }

  private void createGoalDeadlineReminder(LearningGoal learningGoal, LocalDate today) {
    String referenceKey = createGoalDeadlineReferenceKey(learningGoal);

    if (notificationRepository.existsByReferenceKey(referenceKey)) {
      return;
    }

    notificationRepository.save(new Notification(
        learningGoal.getStudyModule().getUser(),
        NotificationType.GOAL_DEADLINE_REMINDER,
        GOAL_DEADLINE_REMINDER_TITLE,
        createGoalDeadlineMessage(learningGoal, today),
        referenceKey));
  }

  private String createGoalDeadlineReferenceKey(LearningGoal learningGoal) {
    return NotificationType.GOAL_DEADLINE_REMINDER + ":" + learningGoal.getId() + ":" + learningGoal.getDeadline();
  }

  private String createGoalDeadlineMessage(LearningGoal learningGoal, LocalDate today) {
    long remainingDays = ChronoUnit.DAYS.between(today, learningGoal.getDeadline());

    if (remainingDays == 0) {
      return "Your learning goal \"" + learningGoal.getTitle() + "\" is due today.";
    }

    if (remainingDays == 1) {
      return "Your learning goal \"" + learningGoal.getTitle() + "\" is due tomorrow.";
    }

    return "Your learning goal \"" + learningGoal.getTitle() + "\" is due in " + remainingDays + " days on " + learningGoal.getDeadline()
                                                                                                                           .format(
                                                                                                                               LearnManagerApplication.DATE_FORMATTER) + ".";
  }
}
