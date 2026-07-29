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
}