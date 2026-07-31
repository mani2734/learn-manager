package com.learnmanager.dto;

import com.learnmanager.entity.StudyTime;

import java.time.LocalDateTime;

public record StudyTimeResponse(Long id, Long studyModuleId, Long learningGoalId, Long plannedStudySessionId, LocalDateTime startTime,
                                LocalDateTime endTime, Long durationMinutes, String notes, LocalDateTime createdAt,
                                LocalDateTime updatedAt) {

  public static StudyTimeResponse fromEntity(StudyTime studyTime) {
    return new StudyTimeResponse(
        studyTime.getId(),
        studyTime.getStudyModule().getId(),
        studyTime.getLearningGoal() != null ? studyTime.getLearningGoal().getId() : null,
        studyTime.getPlannedStudySession() != null ? studyTime.getPlannedStudySession().getId() : null,
        studyTime.getStartTime(),
        studyTime.getEndTime(),
        studyTime.getDurationMinutes(),
        studyTime.getNotes(),
        studyTime.getCreatedAt(),
        studyTime.getUpdatedAt());
  }
}