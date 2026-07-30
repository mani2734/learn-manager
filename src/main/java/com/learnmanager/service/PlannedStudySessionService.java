package com.learnmanager.service;

import com.learnmanager.dto.CreatePlannedStudySessionRequest;
import com.learnmanager.dto.PlannedStudySessionResponse;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.repository.PlannedStudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlannedStudySessionService {

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  private final HelperService helperService;

  @Transactional
  public PlannedStudySessionResponse create(String userEmail, CreatePlannedStudySessionRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    validateTimeRange(request);

    return PlannedStudySessionResponse.fromEntity(plannedStudySessionRepository.save(new PlannedStudySession(
        studyModule.getUser(),
                                                                                                             studyModule,
                                                                                                             request.title().trim(),
                                                                                                             request.startTime(),
                                                                                                             request.endTime())));
  }

  private void validateTimeRange(CreatePlannedStudySessionRequest request) {
    if (!request.endTime().isAfter(request.startTime())) {
      throw new BusinessRuleException("End time must be after start time");
    }
  }
}