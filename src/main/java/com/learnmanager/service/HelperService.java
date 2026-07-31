package com.learnmanager.service;

import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class HelperService {

  private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

  private static final BigDecimal MAXIMUM_PROGRESS = BigDecimal.valueOf(100);

  private final StudyModuleRepository studyModuleRepository;

  private final LearningGoalRepository learningGoalRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final UserRepository userRepository;

  private final PlanningPeriodRepository planningPeriodRepository;

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  public String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  public String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  @Transactional(readOnly = true)
  public StudyModule findOwnedStudyModule(String userEmail, Long studyModuleId) {
    return studyModuleRepository.findByIdAndUserEmailIgnoreCase(studyModuleId, normalizeEmail(userEmail))
                                .orElseThrow(() -> new ResourceNotFoundException("Study module not found"));
  }

  @Transactional(readOnly = true)
  public void validateWorkloadAgainstLearningGoals(StudyModule studyModule, BigDecimal workloadHours, boolean includeNewLearningGoal) {
    BigDecimal assignedWorkload = learningGoalRepository.findAllByStudyModule_Id(studyModule.getId())
                                                        .stream()
                                                        .map(LearningGoal::getWorkloadHours)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (includeNewLearningGoal) {
      BigDecimal resultingWorkload = assignedWorkload.add(workloadHours);

      if (resultingWorkload.compareTo(studyModule.getWorkloadHours()) > 0) {
        BigDecimal availableWorkload = studyModule.getWorkloadHours().subtract(assignedWorkload);

        throw new BusinessRuleException("Learning goal workload exceeds the available module workload. " + "Available workload: " + availableWorkload + " hours");
      }

      return;
    }

    if (workloadHours.compareTo(assignedWorkload) < 0) {
      throw new BusinessRuleException("Module workload must not be lower than the total learning goal workload");
    }
  }

  @Transactional(readOnly = true)
  public LearningGoal findOwnedLearningGoal(String userEmail, Long learningGoalId) {
    return learningGoalRepository.findByIdAndStudyModule_User_EmailIgnoreCase(learningGoalId, normalizeEmail(userEmail))
                                 .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));
  }

  @Transactional(readOnly = true)
  public BigDecimal calculateLearningGoalProgress(
      LearningGoal learningGoal) {
    if (learningGoal.getStatus() == GoalStatus.COMPLETED) {
      return MAXIMUM_PROGRESS.setScale(2);
    }

    long totalMinutes = studyTimeRepository.findAllByLearningGoal_Id(learningGoal.getId())
                                           .stream()
                                           .mapToLong(StudyTime::getDurationMinutes)
                                           .sum();

    if (totalMinutes <= 0) {
      return BigDecimal.ZERO.setScale(2);
    }

    BigDecimal trackedHours = BigDecimal.valueOf(totalMinutes).divide(MINUTES_PER_HOUR, 4, RoundingMode.HALF_UP);

    BigDecimal progress = trackedHours.divide(learningGoal.getWorkloadHours(), 4, RoundingMode.HALF_UP)
                                      .multiply(MAXIMUM_PROGRESS)
                                      .min(MAXIMUM_PROGRESS);

    return progress.setScale(2, RoundingMode.HALF_UP);
  }

  @Transactional(readOnly = true)
  public User findUserByEmail(String userEmail) {
    return userRepository.findByEmailIgnoreCase(normalizeEmail(userEmail))
                         .orElseThrow(() -> new UsernameNotFoundException("Authenticated user no longer exists"));
  }

  @Transactional(readOnly = true)
  public PlanningPeriod findOwnedPlanningPeriod(String userEmail, Long planningPeriodId) {
    return planningPeriodRepository.findByIdAndUser_EmailIgnoreCase(planningPeriodId, normalizeEmail(userEmail))
                                   .orElseThrow(() -> new ResourceNotFoundException("Planning period not found"));
  }

  @Transactional(readOnly = true)
  public PlannedStudySession findOwnedPlannedStudySession(String userEmail, Long plannedStudySessionId) {
    return plannedStudySessionRepository.findByIdAndUser_EmailIgnoreCase(plannedStudySessionId, normalizeEmail(userEmail))
                                        .orElseThrow(() -> new ResourceNotFoundException("Planned study session not found"));
  }

}