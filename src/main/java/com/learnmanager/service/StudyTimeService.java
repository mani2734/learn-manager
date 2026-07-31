package com.learnmanager.service;

import com.learnmanager.dto.CreateStudyTimeRequest;
import com.learnmanager.dto.StudyTimeResponse;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.StudyTime;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.repository.StudyTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudyTimeService {

  private final StudyTimeRepository studyTimeRepository;

  private final HelperService helperService;

  @Transactional
  public StudyTimeResponse create(String userEmail, CreateStudyTimeRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    validateTimeRange(request.startTime(), request.endTime());
    validateNoOverlap(studyModule.getUser().getEmail(), request.startTime(), request.endTime());

    LearningGoal learningGoal = resolveLearningGoal(userEmail, request.learningGoalId(), studyModule.getId());

    PlannedStudySession plannedStudySession = resolvePlannedStudySession(userEmail, request.plannedStudySessionId(), studyModule.getId());

    return StudyTimeResponse.fromEntity(studyTimeRepository.save(new StudyTime(
        studyModule.getUser(),
                                                                               studyModule,
                                                                               learningGoal,
                                                                               plannedStudySession,
                                                                               request.startTime(),
                                                                               request.endTime(),
                                                                               helperService.normalizeOptionalText(request.notes()))));
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

  private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (!endTime.isAfter(startTime)) {
      throw new BusinessRuleException("End time must be after start time");
    }
  }

  private void validateNoOverlap(String userEmail, LocalDateTime startTime, LocalDateTime endTime) {
    boolean overlapsExistingStudyTime = studyTimeRepository.existsByUser_EmailIgnoreCaseAndStartTimeLessThanAndEndTimeGreaterThan(
        userEmail,
        endTime,
        startTime);

    if (overlapsExistingStudyTime) {
      throw new BusinessRuleException("Study time overlaps an existing study time");
    }
  }
}