package com.learnmanager.dto;

import com.learnmanager.entity.ModulePlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ModulePlanResponse(Long id, Long planningPeriodId, Long studyModuleId, Integer periodNumber, BigDecimal plannedHours,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static ModulePlanResponse fromEntity(ModulePlan modulePlan) {
    return new ModulePlanResponse(
        modulePlan.getId(),
        modulePlan.getPlanningPeriod().getId(),
        modulePlan.getStudyModule().getId(),
        modulePlan.getPeriodNumber(),
        modulePlan.getPlannedHours(),
        modulePlan.getCreatedAt(),
        modulePlan.getUpdatedAt());
  }
}