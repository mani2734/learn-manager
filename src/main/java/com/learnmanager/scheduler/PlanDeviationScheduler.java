package com.learnmanager.scheduler;

import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PlanDeviationScheduler {

  private static final int PLANNING_SECTION_LENGTH_DAYS = 30;

  private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

  private static final BigDecimal PERCENTAGE_FACTOR = BigDecimal.valueOf(100);

  private static final String PLAN_DEVIATION_REMINDER_TITLE = "Plan deviation reminder";

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final NotificationRepository notificationRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final PlanningPeriodRepository planningPeriodRepository;

  private final ModulePlanRepository modulePlanRepository;

  private final Clock applicationClock;

  @Scheduled(cron = "0 10 0 * * *", zone = "Europe/Vienna")
  @Transactional
  public void createPlanDeviationReminders() {
    LocalDate evaluationDate = LocalDate.now(applicationClock).minusDays(1);

    notificationSettingsRepository.findAllByPlanDeviationReminderEnabledTrueAndUser_ActiveTrue()
                                  .forEach(notificationSettings -> createPlanDeviationReminders(notificationSettings, evaluationDate));
  }

  private void createPlanDeviationReminders(NotificationSettings notificationSettings, LocalDate evaluationDate) {
    planningPeriodRepository.findFirstByUser_EmailIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(notificationSettings.getUser().getEmail(),
                                                                                                                                    evaluationDate,
                                                                                                                                    evaluationDate)
                            .ifPresent(planningPeriod -> createPlanDeviationReminders(
                                notificationSettings,
                                planningPeriod,
                                evaluationDate));
  }

  private void createPlanDeviationReminders(
      NotificationSettings notificationSettings,
      PlanningPeriod planningPeriod,
      LocalDate evaluationDate) {
    long daysSincePlanningPeriodStart = ChronoUnit.DAYS.between(planningPeriod.getStartDate(), evaluationDate);

    int periodNumber = (int) (daysSincePlanningPeriodStart / PLANNING_SECTION_LENGTH_DAYS) + 1;

    LocalDate sectionStartDate = planningPeriod.getStartDate().plusDays((periodNumber - 1L) * PLANNING_SECTION_LENGTH_DAYS);

    long completedDays = ChronoUnit.DAYS.between(sectionStartDate, evaluationDate) + 1;

    LocalDateTime rangeStart = sectionStartDate.atStartOfDay();
    LocalDateTime rangeEnd = evaluationDate.plusDays(1).atStartOfDay();

    modulePlanRepository.findAllByPlanningPeriod_IdAndPeriodNumberOrderByStudyModule_NameAsc(planningPeriod.getId(), periodNumber)
                        .forEach(modulePlan -> createPlanDeviationReminder(
                            notificationSettings,
                            modulePlan,
                            completedDays,
                            rangeStart,
                            rangeEnd));
  }

  private void createPlanDeviationReminder(
      NotificationSettings notificationSettings,
      ModulePlan modulePlan,
      long completedDays,
      LocalDateTime rangeStart,
      LocalDateTime rangeEnd) {
    BigDecimal plannedMinutes = modulePlan.getPlannedHours().multiply(MINUTES_PER_HOUR);

    BigDecimal expectedMinutes = plannedMinutes.multiply(BigDecimal.valueOf(completedDays))
                                               .divide(BigDecimal.valueOf(PLANNING_SECTION_LENGTH_DAYS), 2, RoundingMode.HALF_UP);

    long actualMinutes = studyTimeRepository.findAllByUser_EmailIgnoreCaseAndStudyModule_IdAndStartTimeLessThanAndEndTimeGreaterThan(notificationSettings.getUser().getEmail(),
                                                                                                                                     modulePlan.getStudyModule()
                                                                                                                                               .getId(),
                                                                                                                                     rangeEnd,
                                                                                                                                     rangeStart)
                                            .stream()
                                            .mapToLong(studyTime -> calculateMinutesWithinRange(studyTime, rangeStart, rangeEnd))
                                            .sum();

    BigDecimal deviationPercentage = calculateDeviationPercentage(expectedMinutes, actualMinutes);

    if (deviationPercentage.compareTo(BigDecimal.valueOf(notificationSettings.getPlanDeviationThresholdPercent())) < 0) {
      return;
    }

    String referenceKey = createPlanDeviationReferenceKey(notificationSettings, modulePlan);

    if (notificationRepository.existsByReferenceKey(referenceKey)) {
      return;
    }

    notificationRepository.save(new Notification(
        notificationSettings.getUser(),
        NotificationType.PLAN_DEVIATION_REMINDER,
        PLAN_DEVIATION_REMINDER_TITLE,
        createPlanDeviationMessage(modulePlan, expectedMinutes, actualMinutes, deviationPercentage),
        referenceKey));
  }

  private BigDecimal calculateDeviationPercentage(BigDecimal expectedMinutes, long actualMinutes) {
    if (expectedMinutes.compareTo(BigDecimal.ZERO) <= 0 || BigDecimal.valueOf(actualMinutes).compareTo(expectedMinutes) >= 0) {
      return BigDecimal.ZERO;
    }

    return expectedMinutes.subtract(BigDecimal.valueOf(actualMinutes))
                          .divide(expectedMinutes, 4, RoundingMode.HALF_UP)
                          .multiply(PERCENTAGE_FACTOR)
                          .setScale(2, RoundingMode.HALF_UP);
  }

  private long calculateMinutesWithinRange(StudyTime studyTime, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    LocalDateTime effectiveStart = studyTime.getStartTime().isBefore(rangeStart) ? rangeStart : studyTime.getStartTime();

    LocalDateTime effectiveEnd = studyTime.getEndTime().isAfter(rangeEnd) ? rangeEnd : studyTime.getEndTime();

    if (!effectiveEnd.isAfter(effectiveStart)) {
      return 0;
    }

    return Duration.between(effectiveStart, effectiveEnd).toMinutes();
  }

  private String createPlanDeviationReferenceKey(NotificationSettings notificationSettings, ModulePlan modulePlan) {
    return NotificationType.PLAN_DEVIATION_REMINDER + ":" + modulePlan.getId() + ":" + modulePlan.getPlannedHours()
                                                                                                 .stripTrailingZeros()
                                                                                                 .toPlainString() + ":" + notificationSettings.getPlanDeviationThresholdPercent();
  }

  private String createPlanDeviationMessage(
      ModulePlan modulePlan,
      BigDecimal expectedMinutes,
      long actualMinutes,
      BigDecimal deviationPercentage) {
    BigDecimal expectedHours = expectedMinutes.divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);

    BigDecimal actualHours = BigDecimal.valueOf(actualMinutes).divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);

    return "You are " + deviationPercentage + "% behind your study plan for \"" + modulePlan.getStudyModule()
                                                                                            .getName() + "\". Expected study time: " + expectedHours + " hours, recorded study time: " + actualHours + " hours.";
  }

}
