package com.learnmanager.dto;

import com.learnmanager.entity.PlanningPeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlanningPeriodResponse(Long id, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static PlanningPeriodResponse fromEntity(
      PlanningPeriod planningPeriod) {
    return new PlanningPeriodResponse(
        planningPeriod.getId(),
        planningPeriod.getStartDate(),
        planningPeriod.getEndDate(),
        planningPeriod.getCreatedAt(),
        planningPeriod.getUpdatedAt());
  }
}