package com.learnmanager.dto;

import com.learnmanager.entity.StudyModule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StudyModuleResponse(Long id, String name, String code, BigDecimal workloadHours,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static StudyModuleResponse fromEntity(
      StudyModule studyModule) {
    return new StudyModuleResponse(
        studyModule.getId(),
        studyModule.getName(),
        studyModule.getCode(),
        studyModule.getWorkloadHours(),
        studyModule.getCreatedAt(),
        studyModule.getUpdatedAt());
  }
}