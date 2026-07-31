package com.learnmanager.dto.response;

import com.learnmanager.entity.PlannedStudySession;

import java.time.LocalDateTime;

public record PlannedStudySessionResponse(Long id, Long studyModuleId, String title, LocalDateTime startTime, LocalDateTime endTime,
                                          LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static PlannedStudySessionResponse fromEntity(PlannedStudySession plannedStudySession) {
    return new PlannedStudySessionResponse(
        plannedStudySession.getId(),
        plannedStudySession.getStudyModule().getId(),
        plannedStudySession.getTitle(),
        plannedStudySession.getStartTime(),
        plannedStudySession.getEndTime(),
        plannedStudySession.getCreatedAt(),
        plannedStudySession.getUpdatedAt());
  }
}