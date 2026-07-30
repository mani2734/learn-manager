package com.learnmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreatePlannedStudySessionRequest(

    @NotNull(message = "Study module ID is required") @Positive(message = "Study module ID must be greater than zero") Long studyModuleId,

    @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must not exceed 150 characters") String title,

    @NotNull(message = "Start time is required") LocalDateTime startTime,

    @NotNull(message = "End time is required") LocalDateTime endTime

) {

}