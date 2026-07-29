package com.learnmanager.service;

import com.learnmanager.dto.CreateStudyModuleRequest;
import com.learnmanager.dto.StudyModuleResponse;
import com.learnmanager.dto.UpdateStudyModuleRequest;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyModuleService {

  private static final BigDecimal HOURS_PER_ECTS = BigDecimal.valueOf(30);

  private final StudyModuleRepository studyModuleRepository;

  private final LearningGoalRepository learningGoalRepository;

  private final UserRepository userRepository;

  private final TimerRepository timerRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final MilestoneRepository milestoneRepository;

  private final ModulePlanRepository modulePlanRepository;

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  @Transactional
  public StudyModuleResponse create(String userEmail, CreateStudyModuleRequest request) {
    User user = userRepository.findByEmailIgnoreCase(userEmail.trim())
                              .orElseThrow(() -> new UsernameNotFoundException("Authenticated user no longer exists"));

    BigDecimal workloadHours = calculateWorkloadHours(request.ects(), request.workloadHours());

    StudyModule studyModule = new StudyModule(
        user,
        request.name().trim(),
        normalizeOptionalText(request.code()),
        normalizeOptionalText(request.description()),
        request.ects(),
        workloadHours);

    StudyModule savedStudyModule = studyModuleRepository.save(studyModule);

    return StudyModuleResponse.fromEntity(savedStudyModule);
  }

  @Transactional(readOnly = true)
  public List<StudyModuleResponse> getAll(String userEmail) {
    return studyModuleRepository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(userEmail.trim())
                                .stream()
                                .map(StudyModuleResponse::fromEntity)
                                .toList();
  }

  @Transactional(readOnly = true)
  public StudyModuleResponse getById(String userEmail, Long moduleId) {
    StudyModule studyModule = findOwnedModule(userEmail, moduleId);

    return StudyModuleResponse.fromEntity(studyModule);
  }

  @Transactional
  public StudyModuleResponse update(String userEmail, Long moduleId, UpdateStudyModuleRequest request) {
    StudyModule studyModule = findOwnedModule(userEmail, moduleId);

    BigDecimal workloadHours = calculateWorkloadHours(request.ects(), request.workloadHours());

    validateWorkloadAgainstLearningGoals(studyModule.getId(), workloadHours);

    studyModule.setName(request.name().trim());
    studyModule.setCode(normalizeOptionalText(request.code()));
    studyModule.setDescription(normalizeOptionalText(request.description()));
    studyModule.setEcts(request.ects());
    studyModule.setWorkloadHours(workloadHours);

    StudyModule updatedStudyModule = studyModuleRepository.save(studyModule);

    return StudyModuleResponse.fromEntity(updatedStudyModule);
  }

  @Transactional
  public void delete(String userEmail, Long moduleId) {
    StudyModule studyModule = findOwnedModule(userEmail, moduleId);

    //don't change the order to avoid foreign key constraint violations
    timerRepository.deleteAllByStudyModule_Id(moduleId);
    studyTimeRepository.deleteAllByStudyModule_Id(moduleId);
    milestoneRepository.deleteAllByLearningGoal_StudyModule_Id(moduleId);
    learningGoalRepository.deleteAllByStudyModule_Id(moduleId);
    modulePlanRepository.deleteAllByStudyModule_Id(moduleId);
    plannedStudySessionRepository.deleteAllByStudyModule_Id(moduleId);
    studyModuleRepository.delete(studyModule);
  }

  private StudyModule findOwnedModule(String userEmail, Long moduleId) {
    return studyModuleRepository.findByIdAndUserEmailIgnoreCase(moduleId, userEmail.trim())
                                .orElseThrow(() -> new ResourceNotFoundException("Study module not found"));
  }

  private BigDecimal calculateWorkloadHours(Integer ects, BigDecimal workloadHours) {
    if (workloadHours != null) {
      return workloadHours;
    }

    return BigDecimal.valueOf(ects).multiply(HOURS_PER_ECTS);
  }

  private void validateWorkloadAgainstLearningGoals(Long studyModuleId, BigDecimal newWorkloadHours) {
    BigDecimal assignedWorkload = learningGoalRepository.findAllByStudyModule_Id(studyModuleId)
                                                        .stream()
                                                        .map(LearningGoal::getWorkloadHours)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (newWorkloadHours.compareTo(assignedWorkload) < 0) {
      throw new BusinessRuleException("Module workload must not be lower than the total learning goal workload");
    }
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }
}