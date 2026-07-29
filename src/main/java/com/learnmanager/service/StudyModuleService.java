package com.learnmanager.service;

import com.learnmanager.dto.CreateStudyModuleRequest;
import com.learnmanager.dto.StudyModuleResponse;
import com.learnmanager.dto.UpdateStudyModuleRequest;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyModuleService {

  private final StudyModuleRepository studyModuleRepository;

  private final LearningGoalRepository learningGoalRepository;

  private final UserRepository userRepository;

  private final TimerRepository timerRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final MilestoneRepository milestoneRepository;

  private final ModulePlanRepository modulePlanRepository;

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  private final HelperService helperService;

  @Transactional
  public StudyModuleResponse create(String userEmail, CreateStudyModuleRequest request) {
    User user = userRepository.findByEmailIgnoreCase(userEmail.trim())
                              .orElseThrow(() -> new UsernameNotFoundException("Authenticated user no longer exists"));

    StudyModule studyModule = new StudyModule(
        user,
        request.name().trim(),
        helperService.normalizeOptionalText(request.code()), request.workloadHours());

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
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, moduleId);

    return StudyModuleResponse.fromEntity(studyModule);
  }

  @Transactional
  public StudyModuleResponse update(String userEmail, Long moduleId, UpdateStudyModuleRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, moduleId);

    helperService.validateWorkloadAgainstLearningGoals(studyModule, request.workloadHours(), false);

    studyModule.setName(request.name().trim());
    studyModule.setCode(helperService.normalizeOptionalText(request.code()));
    studyModule.setWorkloadHours(request.workloadHours());

    StudyModule updatedStudyModule = studyModuleRepository.save(studyModule);

    return StudyModuleResponse.fromEntity(updatedStudyModule);
  }

  @Transactional
  public void delete(String userEmail, Long moduleId) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, moduleId);

    //don't change the order to avoid foreign key constraint violations
    timerRepository.deleteAllByStudyModule_Id(moduleId);
    studyTimeRepository.deleteAllByStudyModule_Id(moduleId);
    milestoneRepository.deleteAllByLearningGoal_StudyModule_Id(moduleId);
    learningGoalRepository.deleteAllByStudyModule_Id(moduleId);
    modulePlanRepository.deleteAllByStudyModule_Id(moduleId);
    plannedStudySessionRepository.deleteAllByStudyModule_Id(moduleId);
    studyModuleRepository.delete(studyModule);
  }
}