package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateLearningGoalRequest;
import com.learnmanager.dto.request.update.UpdateLearningGoalRequest;
import com.learnmanager.dto.response.LearningGoalResponse;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.repository.LearningGoalRepository;
import com.learnmanager.repository.MilestoneRepository;
import com.learnmanager.repository.StudyTimeRepository;
import com.learnmanager.repository.TimerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningGoalService {

  private final LearningGoalRepository learningGoalRepository;

  private final MilestoneRepository milestoneRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final TimerRepository timerRepository;

  private final HelperService helperService;

  @Transactional
  public LearningGoalResponse create(String userEmail, CreateLearningGoalRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    helperService.validateWorkloadAgainstLearningGoals(studyModule, request.workloadHours(), true);

    LearningGoal learningGoal = new LearningGoal(studyModule, request.title().trim(), request.workloadHours(), request.deadline());

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

  @Transactional
  public LearningGoalResponse update(String userEmail, Long learningGoalId, UpdateLearningGoalRequest request) {
    LearningGoal learningGoal = helperService.findOwnedLearningGoal(userEmail, learningGoalId);

    BigDecimal workloadDifference = request.workloadHours().subtract(learningGoal.getWorkloadHours());

    if (workloadDifference.compareTo(BigDecimal.ZERO) > 0) {
      helperService.validateWorkloadAgainstLearningGoals(learningGoal.getStudyModule(), workloadDifference, true);
    }

    learningGoal.setTitle(request.title().trim());
    learningGoal.setWorkloadHours(request.workloadHours());
    learningGoal.setDeadline(request.deadline());
    learningGoal.setStatus(request.status());

    LearningGoal updatedLearningGoal = learningGoalRepository.save(learningGoal);

    return createResponse(updatedLearningGoal);
  }

  @Transactional
  public void delete(String userEmail, Long learningGoalId) {
    LearningGoal learningGoal = helperService.findOwnedLearningGoal(userEmail, learningGoalId);

    timerRepository.deleteAllByLearningGoal_Id(learningGoalId);
    studyTimeRepository.deleteAllByLearningGoal_Id(learningGoalId);
    milestoneRepository.deleteAllByLearningGoal_Id(learningGoalId);
    learningGoalRepository.delete(learningGoal);
  }

  private LearningGoalResponse createResponse(
      LearningGoal learningGoal) {
    BigDecimal progress = helperService.calculateLearningGoalProgress(learningGoal);

    return LearningGoalResponse.fromEntity(learningGoal, progress);
  }
}