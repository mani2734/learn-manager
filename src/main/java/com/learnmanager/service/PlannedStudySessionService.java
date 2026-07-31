package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreatePlannedStudySessionRequest;
import com.learnmanager.dto.request.create.CreatePlannedStudySessionSeriesRequest;
import com.learnmanager.dto.request.update.UpdatePlannedStudySessionRequest;
import com.learnmanager.dto.response.PlannedStudySessionResponse;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.enums.RecurrenceFrequency;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.repository.PlannedStudySessionRepository;
import com.learnmanager.repository.StudyTimeRepository;
import com.learnmanager.repository.TimerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlannedStudySessionService {

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final TimerRepository timerRepository;

  private final HelperService helperService;

  @Transactional
  public PlannedStudySessionResponse create(String userEmail, CreatePlannedStudySessionRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    validateTimeRange(request.startTime(), request.endTime());

    return PlannedStudySessionResponse.fromEntity(plannedStudySessionRepository.save(new PlannedStudySession(
        studyModule.getUser(),
                                                                                                             studyModule,
                                                                                                             request.title().trim(),
                                                                                                             request.startTime(),
                                                                                                             request.endTime())));
  }

  @Transactional(readOnly = true)
  public List<PlannedStudySessionResponse> getAll(String userEmail) {
    return plannedStudySessionRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeAsc(helperService.normalizeEmail(userEmail))
                                        .stream()
                                        .map(PlannedStudySessionResponse::fromEntity)
                                        .toList();
  }

  @Transactional(readOnly = true)
  public List<PlannedStudySessionResponse> getAllByStudyModule(String userEmail, Long studyModuleId) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, studyModuleId);

    return plannedStudySessionRepository.findAllByStudyModule_IdOrderByStartTimeAsc(studyModule.getId())
                                        .stream()
                                        .map(PlannedStudySessionResponse::fromEntity)
                                        .toList();
  }

  @Transactional(readOnly = true)
  public PlannedStudySessionResponse getById(String userEmail, Long plannedStudySessionId) {
    return PlannedStudySessionResponse.fromEntity(helperService.findOwnedPlannedStudySession(userEmail, plannedStudySessionId));
  }

  @Transactional
  public PlannedStudySessionResponse update(String userEmail, Long plannedStudySessionId, UpdatePlannedStudySessionRequest request) {
    PlannedStudySession plannedStudySession = helperService.findOwnedPlannedStudySession(userEmail, plannedStudySessionId);

    validateTimeRange(request.startTime(), request.endTime());

    plannedStudySession.update(request.title().trim(), request.startTime(), request.endTime());

    return PlannedStudySessionResponse.fromEntity(plannedStudySessionRepository.save(plannedStudySession));
  }

  @Transactional
  public void delete(String userEmail, Long plannedStudySessionId) {
    PlannedStudySession plannedStudySession = helperService.findOwnedPlannedStudySession(userEmail, plannedStudySessionId);

    validateCanBeDeleted(plannedStudySessionId);

    plannedStudySessionRepository.delete(plannedStudySession);
  }

  @Transactional
  public List<PlannedStudySessionResponse> createSeries(String userEmail, CreatePlannedStudySessionSeriesRequest request) {
    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    validateTimeRange(request.startTime(), request.endTime());

    List<PlannedStudySession> plannedStudySessions = new ArrayList<>();

    for (int occurrenceIndex = 0; occurrenceIndex < request.occurrenceCount(); occurrenceIndex++) {
      long dayOffset = calculateDayOffset(request.recurrenceFrequency(), occurrenceIndex);

      plannedStudySessions.add(new PlannedStudySession(
          studyModule.getUser(),
          studyModule,
          request.title().trim(),
          request.startTime().plusDays(dayOffset),
          request.endTime().plusDays(dayOffset)));
    }

    return plannedStudySessionRepository.saveAll(plannedStudySessions).stream().map(PlannedStudySessionResponse::fromEntity).toList();
  }

  private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (!endTime.isAfter(startTime)) {
      throw new BusinessRuleException("End time must be after start time");
    }
  }

  private void validateCanBeDeleted(Long plannedStudySessionId) {
    if (timerRepository.existsByPlannedStudySession_Id(plannedStudySessionId)) {
      throw new BusinessRuleException("Planned study session cannot be deleted while an active timer is linked to it");
    }

    if (studyTimeRepository.existsByPlannedStudySession_Id(plannedStudySessionId)) {
      throw new BusinessRuleException("Planned study session cannot be deleted because tracked study time is linked to it");
    }
  }

  private long calculateDayOffset(RecurrenceFrequency recurrenceFrequency, int occurrenceIndex) {
    return switch (recurrenceFrequency) {
      case DAILY -> occurrenceIndex;
      case WEEKLY -> occurrenceIndex * 7L;
    };
  }

}