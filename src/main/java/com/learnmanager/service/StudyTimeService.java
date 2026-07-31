package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateStudyTimeRequest;
import com.learnmanager.dto.request.update.UpdateStudyTimeRequest;
import com.learnmanager.dto.response.StudyTimeResponse;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.StudyTime;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.StudyTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyTimeService {

  private final StudyTimeRepository studyTimeRepository;

  private final HelperService helperService;

  @Transactional
  public StudyTimeResponse create(String userEmail, CreateStudyTimeRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    validateTimeRange(request.startTime(), request.endTime());
    helperService.validateNoStudyTimeOverlap(studyModule.getUser().getEmail(), request.startTime(), request.endTime());

    LearningGoal learningGoal = resolveLearningGoal(userEmail, request.learningGoalId(), studyModule.getId());

    PlannedStudySession plannedStudySession = resolvePlannedStudySession(userEmail, request.plannedStudySessionId(), studyModule.getId());

    return StudyTimeResponse.fromEntity(studyTimeRepository.save(new StudyTime(
        studyModule.getUser(),
                                                                               studyModule,
                                                                               learningGoal,
                                                                               plannedStudySession,
                                                                               request.startTime(), request.endTime())));
  }

  @Transactional(readOnly = true)
  public List<StudyTimeResponse> getAll(String userEmail) {
    return studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(helperService.normalizeEmail(userEmail))
                              .stream()
                              .map(StudyTimeResponse::fromEntity)
                              .toList();
  }

  @Transactional(readOnly = true)
  public List<StudyTimeResponse> getAllByStudyModule(String userEmail, Long studyModuleId) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, studyModuleId);

    return studyTimeRepository.findAllByStudyModule_IdOrderByStartTimeDesc(studyModule.getId())
                              .stream()
                              .map(StudyTimeResponse::fromEntity)
                              .toList();
  }

  @Transactional(readOnly = true)
  public List<StudyTimeResponse> getAllByLearningGoal(String userEmail, Long learningGoalId) {
    LearningGoal learningGoal = helperService.findOwnedLearningGoal(userEmail, learningGoalId);

    return studyTimeRepository.findAllByLearningGoal_IdOrderByStartTimeDesc(learningGoal.getId())
                              .stream()
                              .map(StudyTimeResponse::fromEntity)
                              .toList();
  }

  @Transactional(readOnly = true)
  public StudyTimeResponse getById(String userEmail, Long studyTimeId) {
    return StudyTimeResponse.fromEntity(findOwnedStudyTime(userEmail, studyTimeId));
  }

  private StudyTime findOwnedStudyTime(String userEmail, Long studyTimeId) {
    return studyTimeRepository.findByIdAndUser_EmailIgnoreCase(studyTimeId, helperService.normalizeEmail(userEmail))
                              .orElseThrow(() -> new ResourceNotFoundException("Study time not found"));
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

  @Transactional
  public StudyTimeResponse update(String userEmail, Long studyTimeId, UpdateStudyTimeRequest request) {
    StudyTime studyTime = findOwnedStudyTime(userEmail, studyTimeId);

    validateTimeRange(request.startTime(), request.endTime());
    validateNoOverlapForUpdate(studyTime.getUser().getEmail(), studyTimeId, request.startTime(), request.endTime());

    LearningGoal learningGoal = resolveLearningGoal(userEmail, request.learningGoalId(), studyTime.getStudyModule().getId());

    PlannedStudySession plannedStudySession = resolvePlannedStudySession(
        userEmail,
        request.plannedStudySessionId(),
        studyTime.getStudyModule().getId());

    studyTime.update(
        learningGoal,
        plannedStudySession,
        request.startTime(), request.endTime());

    return StudyTimeResponse.fromEntity(studyTimeRepository.save(studyTime));
  }

  @Transactional
  public void delete(String userEmail, Long studyTimeId) {
    studyTimeRepository.delete(findOwnedStudyTime(userEmail, studyTimeId));
  }

  private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (!endTime.isAfter(startTime)) {
      throw new BusinessRuleException("End time must be after start time");
    }
  }

  private void validateNoOverlapForUpdate(String userEmail, Long studyTimeId, LocalDateTime startTime, LocalDateTime endTime) {
    boolean overlapsExistingStudyTime = studyTimeRepository.existsByUser_EmailIgnoreCaseAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(userEmail,
                                                                                                                                          studyTimeId,
                                                                                                                                          endTime,
                                                                                                                                          startTime);

    if (overlapsExistingStudyTime) {
      throw new BusinessRuleException("Study time overlaps an existing study time");
    }
  }
}