package com.learnmanager.service;

import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.LearningGoalRepository;
import com.learnmanager.repository.StudyModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class HelperService {

  private final StudyModuleRepository studyModuleRepository;

  private final LearningGoalRepository learningGoalRepository;

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
}