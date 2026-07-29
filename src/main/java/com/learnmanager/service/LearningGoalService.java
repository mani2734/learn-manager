package com.learnmanager.service;

import com.learnmanager.dto.CreateLearningGoalRequest;
import com.learnmanager.dto.LearningGoalResponse;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.repository.LearningGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningGoalService {

  private final LearningGoalRepository learningGoalRepository;

  private final HelperService helperService;

  @Transactional
  public LearningGoalResponse create(String userEmail, CreateLearningGoalRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    helperService.validateWorkloadAgainstLearningGoals(studyModule, request.workloadHours(), true);

    LearningGoal learningGoal = new LearningGoal(
        studyModule,
        request.title().trim(),
        helperService.normalizeOptionalText(request.description()),
        request.workloadHours(),
        request.deadline());

    LearningGoal savedLearningGoal = learningGoalRepository.save(learningGoal);

    return LearningGoalResponse.fromEntity(savedLearningGoal, BigDecimal.ZERO);
  }

  @Transactional(readOnly = true)
  public List<LearningGoalResponse> getAll(
      String userEmail) {
    return learningGoalRepository.findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(helperService.normalizeEmail(userEmail))
                                 .stream()
                                 .map(this::createResponse)
                                 .toList();
  }

  @Transactional(readOnly = true)
  public List<LearningGoalResponse> getAllByModule(String userEmail, Long studyModuleId) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, studyModuleId);

    return learningGoalRepository.findAllByStudyModule_IdOrderByCreatedAtDesc(studyModule.getId())
                                 .stream()
                                 .map(this::createResponse)
                                 .toList();
  }

  @Transactional(readOnly = true)
  public LearningGoalResponse getById(String userEmail, Long learningGoalId) {
    LearningGoal learningGoal = helperService.findOwnedLearningGoal(userEmail, learningGoalId);

    return createResponse(learningGoal);
  }

  private LearningGoalResponse createResponse(
      LearningGoal learningGoal) {
    BigDecimal progress = helperService.calculateLearningGoalProgress(learningGoal);

    return LearningGoalResponse.fromEntity(learningGoal, progress);
  }
}