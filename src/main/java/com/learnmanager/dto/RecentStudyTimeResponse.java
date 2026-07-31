package com.learnmanager.dto;

import com.learnmanager.entity.StudyTime;

import java.time.LocalDateTime;

public record RecentStudyTimeResponse(Long studyTimeId, Long studyModuleId, String studyModuleName, Long learningGoalId,
                                      String learningGoalTitle, Long plannedStudySessionId, String plannedStudySessionTitle,
                                      LocalDateTime startTime, LocalDateTime endTime, long durationMinutes) {

  public static RecentStudyTimeResponse fromEntity(StudyTime studyTime) {
    return new RecentStudyTimeResponse(
        studyTime.getId(),
        studyTime.getStudyModule().getId(),
        studyTime.getStudyModule().getName(),
        studyTime.getLearningGoal() != null ? studyTime.getLearningGoal().getId() : null,
        studyTime.getLearningGoal() != null ? studyTime.getLearningGoal().getTitle() : null,
        studyTime.getPlannedStudySession() != null ? studyTime.getPlannedStudySession().getId() : null,
        studyTime.getPlannedStudySession() != null ? studyTime.getPlannedStudySession().getTitle() : null,
        studyTime.getStartTime(),
        studyTime.getEndTime(),
        studyTime.getDurationMinutes());
  }
}