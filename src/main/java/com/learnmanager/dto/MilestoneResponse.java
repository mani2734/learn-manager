package com.learnmanager.dto;

import com.learnmanager.entity.Milestone;
import com.learnmanager.entity.enums.GoalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MilestoneResponse(Long id, Long learningGoalId, String title, LocalDate deadline, GoalStatus status, LocalDateTime createdAt,
                                LocalDateTime updatedAt) {

  public static MilestoneResponse fromEntity(Milestone milestone) {
    return new MilestoneResponse(
        milestone.getId(),
        milestone.getLearningGoal().getId(),
        milestone.getTitle(),
        milestone.getDeadline(),
        milestone.getStatus(),
        milestone.getCreatedAt(),
        milestone.getUpdatedAt());
  }
}