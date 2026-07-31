package com.learnmanager.service;

import com.learnmanager.dto.response.*;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.StudyTime;
import com.learnmanager.repository.PlannedStudySessionRepository;
import com.learnmanager.repository.StudyModuleRepository;
import com.learnmanager.repository.StudyTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

  private static final ZoneId APPLICATION_TIME_ZONE = ZoneId.of("Europe/Vienna");

  private static final BigDecimal PERCENTAGE_FACTOR = BigDecimal.valueOf(100);

  private final StudyModuleRepository studyModuleRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  private final HelperService helperService;

  @Transactional(readOnly = true)
  public ReportingDashboardResponse getDashboard(String userEmail) {
    String normalizedEmail = helperService.normalizeEmail(userEmail);

    LocalDate today = LocalDate.now(APPLICATION_TIME_ZONE);
    LocalDateTime todayStart = today.atStartOfDay();
    LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
    LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
    LocalDateTime nextMonthStart = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

    List<StudyTime> monthlyStudyTimes = studyTimeRepository.findAllByUser_EmailIgnoreCaseAndStartTimeLessThanAndEndTimeGreaterThan(normalizedEmail,
                                                                                                                                   nextMonthStart,
                                                                                                                                   monthStart);

    List<PlannedStudySession> monthlyPlannedStudySessions = plannedStudySessionRepository.findAllByUser_EmailIgnoreCaseAndStartTimeLessThanAndEndTimeGreaterThan(normalizedEmail,
                                                                                                                                                                 nextMonthStart,
                                                                                                                                                                 monthStart);

    long todayLearnedMinutes = monthlyStudyTimes.stream()
                                                .filter(studyTime -> overlaps(
                                                    studyTime.getStartTime(),
                                                    studyTime.getEndTime(),
                                                    todayStart,
                                                    tomorrowStart))
                                                .mapToLong(studyTime -> calculateMinutesWithinRange(
                                                    studyTime.getStartTime(),
                                                    studyTime.getEndTime(),
                                                    todayStart,
                                                    tomorrowStart))
                                                .sum();

    long todaySessionCount = monthlyStudyTimes.stream()
                                              .filter(studyTime -> overlaps(
                                                  studyTime.getStartTime(),
                                                  studyTime.getEndTime(),
                                                  todayStart,
                                                  tomorrowStart))
                                              .count();

    long monthlyLearnedMinutes = monthlyStudyTimes.stream()
                                                  .mapToLong(studyTime -> calculateMinutesWithinRange(
                                                      studyTime.getStartTime(),
                                                      studyTime.getEndTime(),
                                                      monthStart,
                                                      nextMonthStart))
                                                  .sum();

    long monthlyPlannedMinutes = monthlyPlannedStudySessions.stream()
                                                            .mapToLong(plannedStudySession -> calculateMinutesWithinRange(
                                                                plannedStudySession.getStartTime(),
                                                                plannedStudySession.getEndTime(),
                                                                monthStart,
                                                                nextMonthStart))
                                                            .sum();

    Map<Long, Long> learnedMinutesByModule = monthlyStudyTimes.stream().collect(Collectors.groupingBy(
        studyTime -> studyTime.getStudyModule().getId(),
        Collectors.summingLong(studyTime -> calculateMinutesWithinRange(
            studyTime.getStartTime(),
            studyTime.getEndTime(),
            monthStart,
            nextMonthStart))));

    Map<Long, Long> plannedMinutesByModule = monthlyPlannedStudySessions.stream().collect(Collectors.groupingBy(
        plannedStudySession -> plannedStudySession.getStudyModule().getId(),
        Collectors.summingLong(plannedStudySession -> calculateMinutesWithinRange(
            plannedStudySession.getStartTime(),
            plannedStudySession.getEndTime(),
            monthStart,
            nextMonthStart))));

    List<ModuleMonthlySummaryResponse> modules = studyModuleRepository.findAllByUserEmailIgnoreCaseOrderByNameAsc(normalizedEmail)
                                                                      .stream()
                                                                      .map(studyModule -> {
                                                                        long learnedMinutes = learnedMinutesByModule.getOrDefault(studyModule.getId(),
                                                                                                                                  0L);

                                                                        long plannedMinutes = plannedMinutesByModule.getOrDefault(studyModule.getId(),
                                                                                                                                  0L);

                                                                        return new ModuleMonthlySummaryResponse(
                                                                            studyModule.getId(),
                                                                            studyModule.getName(),
                                                                            learnedMinutes,
                                                                            plannedMinutes,
                                                                            calculateProgressPercentage(learnedMinutes, plannedMinutes));
                                                                      })
                                                                      .toList();

    List<RecentStudyTimeResponse> recentSessions = studyTimeRepository.findTop5ByUser_EmailIgnoreCaseOrderByStartTimeDesc(normalizedEmail)
                                                                      .stream()
                                                                      .map(RecentStudyTimeResponse::fromEntity)
                                                                      .toList();

    return new ReportingDashboardResponse(
        today.getYear(),
        today.getMonthValue(),
        new TodayStudySummaryResponse(todayLearnedMinutes, todaySessionCount),
        new MonthlyStudySummaryResponse(
            monthlyLearnedMinutes,
                                        monthlyPlannedMinutes,
                                        calculateProgressPercentage(monthlyLearnedMinutes, monthlyPlannedMinutes)),
        modules,
        recentSessions);
  }

  private long calculateMinutesWithinRange(
      LocalDateTime startTime,
      LocalDateTime endTime,
      LocalDateTime rangeStart,
      LocalDateTime rangeEnd) {
    LocalDateTime effectiveStart = startTime.isBefore(rangeStart) ? rangeStart : startTime;

    LocalDateTime effectiveEnd = endTime.isAfter(rangeEnd) ? rangeEnd : endTime;

    if (!effectiveEnd.isAfter(effectiveStart)) {
      return 0;
    }

    return Duration.between(effectiveStart, effectiveEnd).toMinutes();
  }

  private boolean overlaps(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    return startTime.isBefore(rangeEnd) && endTime.isAfter(rangeStart);
  }

  private BigDecimal calculateProgressPercentage(long learnedMinutes, long plannedMinutes) {
    if (plannedMinutes <= 0) {
      return null;
    }

    return BigDecimal.valueOf(learnedMinutes)
                     .divide(BigDecimal.valueOf(plannedMinutes), 4, RoundingMode.HALF_UP)
                     .multiply(PERCENTAGE_FACTOR)
                     .setScale(2, RoundingMode.HALF_UP);
  }
}