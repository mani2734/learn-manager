package com.learnmanager.dto.response;

import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LearningGoalResponse(Long id, Long studyModuleId, String title, BigDecimal workloadHours,
                                   LocalDate deadline, GoalStatus status, BigDecimal progressPercentage, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {

  public static LearningGoalResponse fromEntity(LearningGoal learningGoal, BigDecimal progressPercentage) {
    return new LearningGoalResponse(
        learningGoal.getId(),
        learningGoal.getStudyModule().getId(),
        learningGoal.getTitle(),
        learningGoal.getWorkloadHours(),
        learningGoal.getDeadline(),
        learningGoal.getStatus(),
        progressPercentage,
        learningGoal.getCreatedAt(),
        learningGoal.getUpdatedAt());
  }
}