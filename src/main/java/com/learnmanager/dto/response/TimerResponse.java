package com.learnmanager.dto.response;

import com.learnmanager.entity.Timer;

import java.time.LocalDateTime;

public record TimerResponse(Long id, Long studyModuleId, Long learningGoalId, Long plannedStudySessionId, LocalDateTime startTime,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static TimerResponse fromEntity(Timer timer) {
    return new TimerResponse(
        timer.getId(),
        timer.getStudyModule().getId(),
        timer.getLearningGoal() != null ? timer.getLearningGoal().getId() : null,
        timer.getPlannedStudySession() != null ? timer.getPlannedStudySession().getId() : null,
        timer.getStartTime(),
        timer.getCreatedAt(),
        timer.getUpdatedAt());
  }
}