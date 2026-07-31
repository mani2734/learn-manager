package com.learnmanager.service;

import com.learnmanager.dto.StartTimerRequest;
import com.learnmanager.dto.TimerResponse;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.Timer;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.TimerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TimerService {

  private final TimerRepository timerRepository;

  private final HelperService helperService;

  @Transactional
  public TimerResponse start(String userEmail, StartTimerRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    validateNoActiveTimer(studyModule.getUser().getEmail());

    LearningGoal learningGoal = resolveLearningGoal(userEmail, request.learningGoalId(), studyModule.getId());

    PlannedStudySession plannedStudySession = resolvePlannedStudySession(userEmail, request.plannedStudySessionId(), studyModule.getId());

    return TimerResponse.fromEntity(timerRepository.save(new Timer(
        studyModule.getUser(),
                                                                   studyModule,
                                                                   learningGoal,
                                                                   plannedStudySession,
                                                                   LocalDateTime.now())));
  }

  @Transactional(readOnly = true)
  public TimerResponse getActive(String userEmail) {
    return TimerResponse.fromEntity(findActiveTimer(userEmail));
  }

  private LearningGoal resolveLearningGoal(String userEmail, Long learningGoalId, Long studyModuleId) {
    if (learningGoalId == null) {
      return null;
    }

    LearningGoal learningGoal = helperService.findOwnedLearningGoal(userEmail, learningGoalId);

    if (!learningGoal.getStudyModule().getId().equals(studyModuleId)) {
      throw new BusinessRuleException("Learning goal does not belong to the selected study module");
    }

    return learningGoal;
  }

  private PlannedStudySession resolvePlannedStudySession(String userEmail, Long plannedStudySessionId, Long studyModuleId) {
    if (plannedStudySessionId == null) {
      return null;
    }

    PlannedStudySession plannedStudySession = helperService.findOwnedPlannedStudySession(userEmail, plannedStudySessionId);

    if (!plannedStudySession.getStudyModule().getId().equals(studyModuleId)) {
      throw new BusinessRuleException("Planned study session does not belong to the selected study module");
    }

    return plannedStudySession;
  }

  private void validateNoActiveTimer(String userEmail) {
    if (timerRepository.existsByUser_EmailIgnoreCase(userEmail)) {
      throw new BusinessRuleException("An active timer already exists");
    }
  }

  private Timer findActiveTimer(String userEmail) {
    return timerRepository.findByUser_EmailIgnoreCase(helperService.normalizeEmail(userEmail))
                          .orElseThrow(() -> new ResourceNotFoundException("Active timer not found"));
  }
}